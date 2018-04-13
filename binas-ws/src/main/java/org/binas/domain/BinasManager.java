package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationView;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BinasManager  {
	private Pattern emailPattern;

	private static String wsName  = null;
	private static String uddiURL = null;

	private static AtomicInteger initialPoints = new AtomicInteger(10);
    private Map<String, User> users = new HashMap<>();
	
	// Singleton -------------------------------------------------------------
	private BinasManager() {
		String namePart = "[A-Za-z0-9]+";
		String nameSep = "\\.";
		String name = namePart + "("+nameSep+namePart+")*";
		String mailSep = "@";
		String EMAIL_PATTERN =	name + mailSep + name;
		this.emailPattern = Pattern.compile(EMAIL_PATTERN); //used to validate user emails

	}

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final BinasManager INSTANCE = new BinasManager();
	}

	public ArrayList<StationClient> findActiveStations() {
		ArrayList<StationClient> activeStationClients = new ArrayList<StationClient>();
		UDDINaming uddiNaming;
		try {
			uddiNaming = new UDDINaming(uddiURL);
			Collection<String> wsURLs = uddiNaming.list(wsName+"%");
			for(String wsURL : wsURLs) {
				activeStationClients.add(new StationClient(wsURL));
			}
		} catch (UDDINamingException e) {
			System.err.println("findActiveStations: UDDINaming Error");
		} catch (StationClientException e) {
			System.err.println("findActiveStations: error creating station client");
		}
        return activeStationClients;
	}

	public StationClient lookupStation(String stationID) {
		StationClient stationClient = null;
		UDDINaming uddiNaming;
		try {
			uddiNaming = new UDDINaming(uddiURL);
			String wsURL = uddiNaming.lookup(stationID);
			if(wsURL == null) return null;
			stationClient = new StationClient(wsURL);
		} catch (UDDINamingException e) {
			System.err.println("findActiveStations: UDDINaming Error");
		} catch (StationClientException e) {
			System.err.println("findActiveStations: error creating station client");
		}
		return stationClient;
	}

	public static synchronized BinasManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void setId(String wsName) {
		if(this.wsName == null)
			this.wsName = wsName;
	}

	public void setUDDIurl(String uddiURL) {
		if(this.uddiURL == null)
			this.uddiURL = uddiURL;
	}

	public User activateUser(String emailAddress) throws EmailExistsException, InvalidEmailException {
		checkEmail(emailAddress);

		User newUser;
		synchronized(users) {
			if(hasEmail(emailAddress)) throw new EmailExistsException();
			newUser = new User(emailAddress, false, initialPoints.get());
			users.put(emailAddress, newUser);
		}
		
		return newUser;
	}
	
	private void checkEmail(String email) throws EmailExistsException, InvalidEmailException {
		if(email == null || email.trim().equals("")) throw new InvalidEmailException();
		
		Matcher matcher = this.emailPattern.matcher(email);
		if(!matcher.matches()) throw new InvalidEmailException();
	}
	
	public boolean hasEmail(String email) {
		return users.containsKey(email);
	}
	
	public User getUser(String email) {
		return users.get(email);
	}

	public void getBina(String stationId, String userEmail)  throws AlreadyHasBinaException,
			InvalidStationException, NoBinaAvailException, NoCreditException, UserNotExistsException {

			User user = getUser(userEmail);
			if(user == null) throw new UserNotExistsException();
			
			synchronized(user) {
				if(user.getCredit() < 1) throw new NoCreditException();
				if(user.hasBina()) throw  new AlreadyHasBinaException();
				
				StationClient stationClient = lookupStation(stationId);
				if(stationClient == null) throw new InvalidStationException();
				
				try {
					stationClient.getBina();
				} catch (NoBinaAvail_Exception e) {
					throw new NoBinaAvailException("");
				}
				
				user.setHasBina(true);
				user.setCredit(user.getCredit()-1);
			}
	}

	public void returnBina(String stationId, String userEmail)
			throws FullStationException, InvalidStationException,NoBinaRentedException, UserNotExistsException {

		User user = getUser(userEmail);
		if(user == null) throw new UserNotExistsException();
		
		synchronized(user) {
			if(!user.hasBina()) throw new NoBinaRentedException();

			StationClient stationClient = lookupStation(stationId);
			if(stationClient == null) throw new InvalidStationException();

			int bonus;
			try {
				bonus = stationClient.returnBina();
			} catch (NoSlotAvail_Exception e) {
				throw new FullStationException();
			}
			
			user.setHasBina(false);
			user.setCredit(user.getCredit() + bonus);
		}
	}


	public int getCredit(String userEmail) throws UserNotExistsException {
		User user = users.get(userEmail);
		if(user == null) throw new UserNotExistsException();
		return user.getCredit();
	}

	// test methods -------

	public void testInit(int userInitialPoints) throws BadInitException {
		if(userInitialPoints >= 0) {
			initialPoints.set(userInitialPoints);
		} else {
			throw new BadInitException("initial points must be non negative");
		}
	}


	public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInitException {
		StationClient station = lookupStation(stationId);
		if(station == null) {
			throw new BadInitException("testInitStation: station could not be reached");
		}
		try {
			station.testInit(x, y, capacity, returnPrize);
			station.testClear();
		} catch (BadInit_Exception e) {
			throw new BadInitException("testInitStation: invalid parameters!");
		}

	}


	public void testClear() {
		synchronized (users) {
		    users.clear();
        }
        for (StationClient stationClient: findActiveStations()) {
			stationClient.testClear();
		}
	}

}
