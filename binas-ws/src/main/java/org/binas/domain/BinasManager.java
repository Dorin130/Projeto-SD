package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.GetBalanceResponse;
import org.binas.station.ws.SetBalanceResponse;
import org.binas.station.ws.UserReplica;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.station.ws.BadInit_Exception;
import org.binas.ws.UserNotExists;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import javax.xml.ws.Response;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BinasManager  {

	private Pattern emailPattern;

	private static String wsName  = null;
	private static String uddiURL = null;
	private static AtomicInteger initialPoints = new AtomicInteger(10);
    private Map<String, User> users = new HashMap<>();

	private final static int POLLING_RATE = 100;
	private AtomicLong seq = new AtomicLong(0l);

	private final static int NUM_STATIONS = 3;

	/**
	 * Sequence number counter.
	 * **/
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

	public static StationClient lookupStation(String stationID) {
		if(stationID == null) return null;
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
	    User user = activateUser(emailAddress, initialPoints.get());

        //quorumSetBalance(emailAddress, initialPoints.get());

        return user;
    }

	public User activateUser(String emailAddress, int points) throws EmailExistsException, InvalidEmailException {
		checkEmail(emailAddress);

		User newUser;
		synchronized(users) { //map.put is harmless if used twice with same email, but second put must throw EmailExistsException
			if(hasEmail(emailAddress)) throw new EmailExistsException();
            try { //Binas Manager might have crashed, need to check stations to confirm that user doesn't exist
                quorumGetBalance(emailAddress);
                throw new EmailExistsException();
            } catch (UserNotExistsException unee) {
                newUser = new User(emailAddress, false, points);
                users.put(emailAddress, newUser);
            }
		}
		
		return newUser;
	}
	
	private void checkEmail(String email) throws InvalidEmailException {
		if(email == null || email.trim().equals("")) throw new InvalidEmailException();
		
		Matcher matcher = this.emailPattern.matcher(email);
		if(!matcher.matches()) throw new InvalidEmailException();
	}
	
	public boolean hasEmail(String email) {
		return users.containsKey(email);
	}
	
	public User getUser(String email) throws UserNotExistsException {
        User user = users.get(email);
        if(user == null) {
            int val = quorumGetBalance(email);
            user = new User(email, false, val); //BinasManager Crashed and user exists in stations
            users.put(email, user);
        }
	    return user;
	}

	public void getBina(String stationId, String userEmail)  throws AlreadyHasBinaException,
		InvalidStationException, NoBinaAvailException, NoCreditException, UserNotExistsException {

		User user = getUser(userEmail);
		//if(user == null) throw new UserNotExistsException();    //unnecessary, covered by getUser, --> TODO: CHECK IF THIS IS REALLY THE CASE

		user.getBina(stationId);
	}

	public void returnBina(String stationId, String userEmail)  throws FullStationException,
            InvalidStationException, NoBinaRentedException, UserNotExistsException {

		User user = getUser(userEmail);
		//if(user == null) throw new UserNotExistsException();    //unnecessary, covered by getUser, --> TODO: CHECK IF THIS IS REALLY THE CASE
		
		user.returnBina(stationId);
	}


	public int getCredit(String userEmail) throws UserNotExistsException {
		User user = getUser(userEmail);
		return user.getCredit();
	}

	// test methods
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

	public UserReplica buildUserReplica(long seq, String email, int points) {
		UserReplica replica = new UserReplica();
		replica.setEmail(email);
		replica.setPoints(points);
		replica.setSeq(seq);
		return replica;
	}

	public void quorumSetBalance(String email, int points) throws ExecutionException, InterruptedException {
		long seq = this.seq.getAndIncrement();
		int i = 0;
		BinasManager bm = BinasManager.getInstance();
		List<Response<SetBalanceResponse>> pending = new ArrayList<>();
		for (StationClient station: bm.findActiveStations()) {
			System.out.println(String.format("CALL %d (%s) setBalanceAsync: %d, %s, %d", i++, station.getWsURL(), seq, email, points));
			pending.add(station.setBalanceAsync(buildUserReplica(seq, email, points)));
		}

		Set<SetBalanceResponse> goodResponses = new HashSet<>();
		while(goodResponses.size() < NUM_STATIONS/2 +1) {
            System.out.println("-----Sleeping-----");
            Thread.sleep(POLLING_RATE);
			i=0;
			for(Response<SetBalanceResponse> response : pending) {
				if (response.isDone()) {
					System.out.println(String.format("RESPONSE %d setBalanceAsync: OK", i));
                    goodResponses.add(response.get());
				}
				i++;
			}
		}
	}

	public int quorumGetBalance(String email) throws UserNotExistsException {
		long MaxSeq = -1;
		int points = -1;
		int i = 0;
		BinasManager bm = BinasManager.getInstance();
		List<Response<GetBalanceResponse>> pending = new ArrayList<>();

		//get all needed responses
		for (StationClient station: bm.findActiveStations()) {
			System.out.println(String.format("CALL %d (%s) GetBalanceAsync: %s ", i++, station.getWsURL(), email));
			pending.add(station.getBalanceAsync(email));
		}

        int responsesNo = 0;
        Set<GetBalanceResponse> goodResponses = new HashSet<>();
		while(goodResponses.size() < NUM_STATIONS/2 +1) {
		    if(responsesNo == pending.size())
		        throw new UserNotExistsException(); //Only possible reason is 2 or more InvalidUser_Exception
            System.out.println("-----Sleeping-----");
            try {
                Thread.sleep(POLLING_RATE);
            } catch (InterruptedException ie) {
                System.out.println("Thread was interrupted while sleeping");
            }
			i = 0; responsesNo = 0;
			for(Response<GetBalanceResponse> response : pending) {
				if (response.isDone()) {
				    try{
				        goodResponses.add(response.get());
                        System.out.println(String.format("RESPONSE %d setBalanceAsync: OK", i));
                        responsesNo++;
                    } catch (ExecutionException ee) {
                        System.out.println(String.format("RESPONSE %d setBalanceAsync: Exception thrown", i));
                        responsesNo++;
                    } catch (InterruptedException ie) {
                        System.out.println(String.format("RESPONSE %d setBalanceAsync: Thread Interrupted", i));
                        /*responsesNo++; does is keep throwing interrupted exception?
                        if so then responsesNo++, else try again like it is doing*/
                    }
				}
				i++;
			}
		}

		//Find biggest sequence number
		long currentSeq;
		for(GetBalanceResponse response : goodResponses) {
			currentSeq = response.getReturn().getSeq();
			if(currentSeq > MaxSeq) {
				MaxSeq = currentSeq;
				points = response.getReturn().getPoints();
			}
		}

		if(points == -1 || MaxSeq == -1) {
			//TODO: error handling here or at the function that calls this?
		}
		return points;

	}

}
