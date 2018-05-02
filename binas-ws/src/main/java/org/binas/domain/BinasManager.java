package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.*;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.ws.UserView;
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
    private final Map<String, User> users = new HashMap<>();

	private final static int POLLING_RATE = 100;
	private AtomicLong seq = new AtomicLong(0l);

	private final static int NUM_STATIONS = 3; //MUST BE int

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


	public ArrayList<StationClient> findActiveStations() {
		ArrayList<StationClient> activeStationClients = new ArrayList<StationClient>();
		UDDINaming uddiNaming;
		try {
			Collection<String> wsURLs;
			uddiNaming = new UDDINaming(uddiURL);
			wsURLs = uddiNaming.list(wsName + "%");
			for(String wsURL : wsURLs) {
				activeStationClients.add(new StationClient(wsURL));
			}
		} catch (UDDINamingException e) {
			System.err.println("findActiveStations: UDDINaming Error");
			return null;
		} catch (StationClientException e) {
			System.err.println("findActiveStations: error creating station client");
			return null;
		}

        return activeStationClients;
	}

	public StationClient lookupStation(String stationID) {
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

	/**
	 * Creates a local user if it doesn't exist already, checking for replicated versions
	 * Ensures that new user data is replicated (write quorum to stations)
	 * @param emailAddress
	 * @return a UserView of the user
	 * @throws EmailExistsException
	 * @throws InvalidEmailException
	 */
    public UserView activateUser(String emailAddress) throws EmailExistsException, InvalidEmailException {
		return activateUser(emailAddress, initialPoints.get());
    }

    /**
	 *  Creates a local User if it doesn't exist already, checking for replicated versions
	 *  New users are NOT replicated (no writes to stations)
	 *  @param emailAddress
	 *  @param points
	 *  @return a UserView of the user
	 */
	private UserView activateUser(String emailAddress, int points) throws EmailExistsException, InvalidEmailException {
		checkEmail(emailAddress);

		//Quick cancel for efficiency
		if(hasEmail(emailAddress)) throw new EmailExistsException();

		User newUser = new User(emailAddress, false, points);

		synchronized (newUser) {
			try { //Need to check stations to confirm that user doesn't exist
				quorumGetBalance(emailAddress);
				throw new EmailExistsException();

			} catch (UserNotExistsException une) {
				synchronized (users) {
					if(hasEmail(emailAddress)) throw new EmailExistsException();
					users.put(emailAddress, newUser);
				}
			}

			quorumSetBalance(emailAddress, points, newUser.getAndIncrementSeq());
			return buildUserView(newUser);
		}
	}
	
	private void checkEmail(String email) throws InvalidEmailException {
		if(email == null || email.trim().equals("")) throw new InvalidEmailException();
		
		Matcher matcher = this.emailPattern.matcher(email);
		if(!matcher.matches()) throw new InvalidEmailException();
	}
	
	public boolean hasEmail(String email) {
		return users.containsKey(email);
	}
	
	private User getUser(String email) throws UserNotExistsException {
        User user = users.get(email);
        if(user == null) {
			UserReplica freshestReplica;

			try {
				freshestReplica = quorumGetBalance(email);
			} catch (UserNotExistsException une) { //catching it here so that it is more explicit
				System.out.println("getUser: UserNotExistsException received from read quorum.");
				throw une;
			}
			//BinasManager crashed and user exists in stations
			user = new User(email, false, freshestReplica.getPoints(), freshestReplica.getSeq()+1);
            users.put(email, user);
        }
	    return user;
	}

	public void getBina(String stationId, String userEmail)  throws AlreadyHasBinaException,
		InvalidStationException, NoBinaAvailException, NoCreditException, UserNotExistsException {

		User user = getUser(userEmail);

		user.getBina(stationId);
	}

	public void returnBina(String stationId, String userEmail)  throws FullStationException,
            InvalidStationException, NoBinaRentedException, UserNotExistsException {

		User user = getUser(userEmail);

		user.returnBina(stationId);
	}


	public int getCredit(String userEmail) throws UserNotExistsException {
		User user = getUser(userEmail);

		return user.getCredit();
	}

	// test methods
	public void testInit(int userInitialPoints) throws BadInitException {
		if(userInitialPoints >= 0) {
			initialPoints.set(userInitialPoints); //atomic
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
		List<StationClient> activeStations = findActiveStations();
		if(activeStations == null) {
			System.out.println("testClear: findActiveStations failed. Cannot clear stations");
			return;
		}
        for (StationClient stationClient: activeStations) {
			stationClient.testClear();
		}
	}

	public void quorumSetBalance(String email, int points, long seq) {

		//Calling the setBalanceAsync method in all the stations
		ArrayList<StationClient> activeStations = findActiveStations();
		if(activeStations == null) {
			System.out.println("quorumSetBalance: findActiveStations failed. Data was not replicated.");
			return;
		}

		List<Response<SetBalanceResponse>> pending = new ArrayList<>();
		for (StationClient station: activeStations) {
			System.out.println(String.format("CALL(%s) setBalanceAsync: %d, %s, %d", station.getWsURL(), seq, email, points));
			pending.add(station.setBalanceAsync(buildUserReplica(seq, email, points)));
		}

		//Polling for all responses
		Set<Response<SetBalanceResponse>> doneResponses = new HashSet<>();
		while(doneResponses.size() < NUM_STATIONS/2 + 1) {
			sleep(POLLING_RATE, "RESPONSE setBalanceAsync: Thread interrupted while sleeping");
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

	private UserReplica quorumGetBalance(String email) throws UserNotExistsException {
		BinasManager bm = BinasManager.getInstance();
		List<StationClient> activeStations = bm.findActiveStations();

		if(activeStations == null) {
			System.out.println("quorumSetBalance: findActiveStations failed. Data was not replicated.");
			return buildUserReplica(-1, "", -1); //TODO CHECK THIS
		}

		if(activeStations.size() < NUM_STATIONS/2 + 1) {
			System.out.println("Not enough stations up for majority quorum");
			return buildUserReplica(-1, "", -1); //TODO CHECK THIS
		}

		int i = 0;
		List<Response<GetBalanceResponse>> pending = new ArrayList<>();
		for (StationClient station: activeStations) {	//send query to each station (asynchronous)
			System.out.println(String.format("CALL %d (%s) GetBalanceAsync: %s ", i++, station.getWsURL(), email));
			pending.add(station.getBalanceAsync(email));
		}

		//Polling for all responses
		Set<Response<GetBalanceResponse>> doneResponses = new HashSet<>();
		while(doneResponses.size() < NUM_STATIONS/2 + 1) {
			sleep(POLLING_RATE, "RESPONSE getBalanceAsync: Thread interrupted while sleeping");
			for(Response<GetBalanceResponse> response : pending) {
				if(response.isDone()) {
					doneResponses.add(response);
				}
			}
		}

		List<GetBalanceResponse> goodResponses = new ArrayList<>();
		for(Response<GetBalanceResponse> response: doneResponses) {
			try {
				goodResponses.add(response.get());
				System.out.println("RESPONSE getBalanceAsync: OK");
			} catch (ExecutionException e) {
				if(e.getCause() instanceof InvalidUser_Exception) { //Valid answer, station might not know about the user
					System.out.println("RESPONSE getBalanceAsync: Invalid User");
				}
			} catch (InterruptedException e) {
				System.out.println("RESPONSE getBalanceAsync: Thread Interrupted, skipping...");
			}
		}

		UserReplica freshestReplica = goodResponses.stream().map(GetBalanceResponse::getReturn)
				.max(Comparator.comparing(UserReplica::getSeq)).orElseThrow( () -> new UserNotExistsException() );

		return freshestReplica;
	}

	// Exception handling helpers --------------------------------------------

	/** Helper for sleep */
	private void sleep(int mil, String interruptMsg) {
		try {
			Thread.sleep(mil);
		} catch (InterruptedException ie) {
			System.out.println(interruptMsg);
			Thread.currentThread().interrupt(); //TODO: check if this is ok.
		}
	}

    // View/Constructor helpers ----------------------------------------------------------

	/** Helper to create a UserReplica. */
	private UserReplica buildUserReplica(long seq, String email, int points) {
		UserReplica replica = new UserReplica();
		replica.setEmail(email);
		replica.setPoints(points);
		replica.setSeq(seq);
		return replica;
	}

    /** Helper to convert user to a user view. */
    private UserView buildUserView(User user) {
        UserView userView = new UserView();
        userView.setEmail(user.getEmail());
        userView.setHasBina(user.hasBina());
        userView.setCredit(user.getCredit());
        return userView;
    }
}