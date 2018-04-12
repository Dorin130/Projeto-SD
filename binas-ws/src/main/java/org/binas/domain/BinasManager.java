package org.binas.domain;

import org.binas.domain.exception.BadInitException;
import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.station.ws.BadInit_Exception;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import java.util.*;


public class BinasManager  {
    private static final String wsName  = "A17_Station";

	private static int initialPoints = 10;
    private Map<String, User> users = new HashMap<>();
	private String uddiURL = null;
	
	// Singleton -------------------------------------------------------------
	private BinasManager() {

	}

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final BinasManager INSTANCE = new BinasManager();
	}

	public synchronized ArrayList<StationClient> findActiveStations() {
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

	public static synchronized BinasManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void setId(String wsName) {
		// TODO Auto-generated method stub
	}
	
	//Obter informaçao feito no impl
	public synchronized User activateUser(String emailAddress) throws EmailExistsException, InvalidEmailException {
		//TODO invalid email address throw
		if(hasEmail(emailAddress)) throw new EmailExistsException();
		User newUser = new User(emailAddress, false, initialPoints);
		users.put(emailAddress, newUser);
		return newUser;
		
	}
	
	public boolean hasEmail(String email) {
		return users.containsKey(email);
	}
	public User getUser(String email) {
			return users.get(email);
	}

	public synchronized void getBina(String stationId, String userEmail)  throws AlreadyHasBinaException,
			InvalidStationException, NoBinaAvailException, NoCreditException, UserNotExistsException {

			if(!hasEmail(userEmail)) throw new UserNotExistsException();

			StationClient stationClient = getStationClient(stationId);
			if(stationClient == null) throw new InvalidStationException();

			User user = getUser(userEmail);
			if(user.getCredit() < 1) throw new NoCreditException();
			if(user.hasBina()) throw  new AlreadyHasBinaException();

			try {
				stationClient.getBina();
			} catch (NoBinaAvail_Exception e) {
				throw new NoBinaAvailException("");
			}
			user.setHasBina(true);
			user.setCredit(user.getCredit()-1);
	}

	public synchronized  void returnBina(String stationId, String userEmail)
			throws FullStationException, InvalidStationException,NoBinaRentedException, UserNotExistsException {

		if(!hasEmail(userEmail)) throw new UserNotExistsException();

		User user = getUser(userEmail);
		if(!user.hasBina()) throw new NoBinaRentedException();

		StationClient stationClient = getStationClient(stationId);
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
	
	public StationClient getStationClient(String stationId) {
		StationClient stationClient = null;
		try {
			stationClient = new StationClient(uddiURL, stationId);
		} catch (StationClientException e) {
			System.err.println("Error initializing station client.");
			System.err.println("Included message: " + e.getMessage());
		}
        return stationClient;
	}

	public synchronized int getCredit(String userEmail) throws UserNotExistsException {
		if(!hasEmail(userEmail)) throw new UserNotExistsException();
		return users.get(userEmail).getCredit();
	}

	public void setUDDIurl(String uddiURL) {
		if(this.uddiURL == null)
			this.uddiURL = uddiURL;
	}

	// test methods -------

	public void testInit(int userInitialPoints) throws BadInitException {
		if(userInitialPoints >= 0) {
			initialPoints = userInitialPoints;
		} else {
			throw new BadInitException("initial points must be non negative");
		}
	}


	public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInitException {
		StationClient station = getStationClient(stationId);
		if(station == null) {
			throw new BadInitException("testInitStation: station could not be reached");
		}
		try {
			station.testInit(x, y, capacity, returnPrize);
		} catch (BadInit_Exception e) {
			throw new BadInitException("testInitStation: invalid parameters!");
		}

	}


	public synchronized void testClear() {
		users.clear();
	}


}
