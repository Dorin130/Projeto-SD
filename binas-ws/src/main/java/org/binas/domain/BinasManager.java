package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.*;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.ws.UserView;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import javax.xml.ws.Response;
import java.lang.reflect.Array;
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
    private final Map<String, User> users = new HashMap<>();

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
			Collection<String> wsURLs;
			try {
				uddiNaming = new UDDINaming(uddiURL);
				wsURLs = uddiNaming.list(wsName + "%");
			} catch (UDDINamingException ue) {
				sleep(100);
				System.out.println("PREVENTED");
				uddiNaming = new UDDINaming(uddiURL);
				wsURLs = uddiNaming.list(wsName + "%");
			}
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

	private void sleep(int milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	public StationClient lookupStation(String stationID) {
		if(stationID == null) return null;
		StationClient stationClient = null;
		UDDINaming uddiNaming;
		try {
			uddiNaming = new UDDINaming(uddiURL);
			String wsURL;
			try {
				uddiNaming = new UDDINaming(uddiURL);
				wsURL = uddiNaming.lookup(stationID);
			} catch (UDDINamingException ue) {
				sleep(100);
				System.out.println("PREVENTED");
				uddiNaming = new UDDINaming(uddiURL);
				wsURL = uddiNaming.lookup(stationID);
			}
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

    public UserView activateUser(String emailAddress) throws EmailExistsException, InvalidEmailException {
	    UserView userView = activateUser(emailAddress, initialPoints.get());
        try {
            quorumSetBalance(emailAddress, initialPoints.get());
        } catch (InterruptedException ie) {
            System.out.println("Thread was interrupted while executing quorumGetBalance. Terminating gracefully.");
            Thread.currentThread().interrupt();
        }
        return userView;
    }

	private UserView activateUser(String emailAddress, int points) throws EmailExistsException, InvalidEmailException {
		checkEmail(emailAddress);

		synchronized(users) { //map.put is harmless if used twice with same email, but second put must throw EmailExistsException
			if(hasEmail(emailAddress)) throw new EmailExistsException();
            try { //Binas Manager might have crashed, need to check stations to confirm that user doesn't exist
                quorumGetBalance(emailAddress);
                throw new EmailExistsException();

            } catch (UserNotExistsException une) {
               User newUser = new User(emailAddress, false, points);
                users.put(emailAddress, newUser);
                return buildUserView(newUser);

            } catch (InterruptedException e) {
				System.out.println("Thread was interrupted while executing quorumGetBalance. Terminating gracefully.");
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}
	
	private void checkEmail(String email) throws InvalidEmailException {
		if(email == null || email.trim().equals("")) throw new InvalidEmailException();
		
		Matcher matcher = this.emailPattern.matcher(email);
		if(!matcher.matches()) throw new InvalidEmailException();
	}
	
	public boolean hasEmail(String email) {
		return users.containsKey(email);
	}
	
	private User getUser(String email) {
        User user = users.get(email);
        if(user == null) {
			int val = 0;

			try {
				val = quorumGetBalance(email);
			} catch (UserNotExistsException e) { //catching it here so that it is more explicit
				return null; //User doesn't exist in BinasManager and isn't replicated in the stations
			} catch (InterruptedException e) {
				System.out.println("Thread was interrupted while executing quorumGetBalance. Terminating gracefully.");
				Thread.currentThread().interrupt(); //TODO: check this (we should respond to binas-cli)
			}

			user = new User(email, false, val); //BinasManager Crashed and user exists in stations
            users.put(email, user);
        }
	    return user;
	}

	public void getBina(String stationId, String userEmail)  throws AlreadyHasBinaException,
		InvalidStationException, NoBinaAvailException, NoCreditException, UserNotExistsException {

		User user = getUser(userEmail);
		if(user == null) throw  new UserNotExistsException(); //It is more explicit to catch the exception here than to let it propagate

		user.getBina(stationId);
	}

	public void returnBina(String stationId, String userEmail)  throws FullStationException,
            InvalidStationException, NoBinaRentedException, UserNotExistsException {

		User user = getUser(userEmail);
		if(user == null) throw new UserNotExistsException();
		
		user.returnBina(stationId);
	}


	public int getCredit(String userEmail) throws UserNotExistsException {
		User user = getUser(userEmail);
		if(user == null) throw new UserNotExistsException();
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
		    seq.set(0);
        }
        for (StationClient stationClient: findActiveStations()) {
			stationClient.testClear();
		}
	}

	private UserReplica buildUserReplica(long seq, String email, int points) {
		UserReplica replica = new UserReplica();
		replica.setEmail(email);
		replica.setPoints(points);
		replica.setSeq(seq);
		return replica;
	}



	/* - -Throws interruptedException because when this exception occurs it means someone is trying to end the execution,
	 (e.g ctrl + c) therefore it is a bad idea to catch it here.
	 - - No need to verify if the user doesn't exist, this is a set
	 */
	public void quorumSetBalance(String email, int points) throws InterruptedException { //TODO verify if it is best to throw  this exception or to handle it above
		long seq = this.seq.getAndIncrement();
		List<Response<SetBalanceResponse>> pending = new ArrayList<>();
		Set<Response<SetBalanceResponse>> doneResponses = new HashSet<>();


		//Calling the setBalanceAsync method in all the stations
		ArrayList<StationClient> activeStations = findActiveStations();
		for (StationClient station: activeStations) {
			System.out.println(String.format("CALL(%s) setBalanceAsync: %d, %s, %d", station.getWsURL(), seq, email, points));
			pending.add(station.setBalanceAsync(buildUserReplica(seq, email, points)));
		}



		//Pooling for all responses
		while(doneResponses.size() < NUM_STATIONS/2 + 1) {
			Thread.sleep(POLLING_RATE);
			for(Response<SetBalanceResponse> response : pending) {
				if(response.isDone()) {
					doneResponses.add(response);
				}
			}
		}

		for(int i = 0; i < doneResponses.size(); i++) {
			System.out.println("RESPONSE setBalanceAsync: OK");
		}

	}

	private int quorumGetBalance(String email) throws UserNotExistsException, InterruptedException {
		BinasManager bm = BinasManager.getInstance();
		List<Response<GetBalanceResponse>> pending = new ArrayList<>();
		int i = 0;

		//get all needed responses
		List<StationClient> stationClients = bm.findActiveStations();

		if(stationClients.size() < NUM_STATIONS/2 + 1) {
			System.out.println("Not enough stations up for majority quorum");
			throw new InterruptedException();
		}

		for (StationClient station: stationClients) {
			System.out.println(String.format("CALL %d (%s) GetBalanceAsync: %s ", i++, station.getWsURL(), email));
			pending.add(station.getBalanceAsync(email));
		}

		//Polling for all responses

		Set<Response<GetBalanceResponse>> doneResponses = new HashSet<>();

		while(doneResponses.size() < NUM_STATIONS/2 + 1) {
			//Only possible reason is 2 or more InvalidUser_Exception
			Thread.sleep(POLLING_RATE);
			for(Response<GetBalanceResponse> response : pending) {
				if(response.isDone()) {
					doneResponses.add(response);
				}
			}
		}

		Set<GetBalanceResponse> goodResponses = new HashSet<>();

		for(Response<GetBalanceResponse> response: doneResponses) {
			try {
				goodResponses.add(response.get());
				System.out.println("RESPONSE getBalanceAsync: OK");
			} catch (ExecutionException e) {
				if(e.getCause() instanceof InvalidUser_Exception) { //Valid answer, station might not know about the user
					System.out.println("RESPONSE getBalanceAsync: Invalid User");
				}
			}
		}

		UserReplica freshestReplica = goodResponses.stream().map(GetBalanceResponse::getReturn)
				.max(Comparator.comparing(UserReplica::getSeq)).orElseThrow( () -> new UserNotExistsException() );

		//attempt to make quorum work with global seq. TODO: check if correct
		seq.accumulateAndGet(freshestReplica.getSeq()+1, Long::max); //updates to biggest value (only updates if BM had crashed

		return freshestReplica.getPoints();
	}


	/*
	* 					} catch (ExecutionException e) {
						if(e.getCause() instanceof InvalidUser_Exception) { //Valid answer, station might not know about the user
							doneResponses.add(response);
							System.out.println("RESPONSE setBalanceAsync: Invalid User exception thrown");
						}
						else { System.out.println("Unknown exception occured"); }//Other exception occurred (e.g WS timeout)
					}
	* */
    // View helpers ----------------------------------------------------------

    /** Helper to convert user to a user view. */
    private UserView buildUserView(User user) {
        UserView userView = new UserView();
        userView.setEmail(user.getEmail());
        userView.setHasBina(user.hasBina());
        userView.setCredit(user.getCredit());
        return userView;
    }
}