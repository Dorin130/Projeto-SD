package org.binas.ws;

import org.binas.domain.BinasManager;
import org.binas.domain.CoordinatesComparator;
import org.binas.domain.User;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.jws.WebService;
import javax.xml.ws.Response;

import org.binas.domain.exception.BadInitException;
import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.FullStationException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.InvalidStationException;
import org.binas.domain.exception.NoBinaRentedException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.domain.exception.*;
import org.binas.station.ws.GetBalance;
import org.binas.station.ws.GetBalanceResponse;
import org.binas.station.ws.SetBalanceResponse;
import org.binas.station.ws.UserReplica;
import org.binas.station.ws.cli.StationClient;

/**
 * This class implements the Web Service port type (interface). The annotations
 * below "map" the Java class to the WSDL definitions.
 */
@WebService(endpointInterface = "org.binas.ws.BinasPortType",
        wsdlLocation = "binas.1_0.wsdl",
        name ="BinasWebService",
        portName = "BinasPort",
        targetNamespace="http://ws.binas.org/",
        serviceName = "BinasService"
)
public class BinasPortImpl implements BinasPortType {

    /**
     * Sequence number counter.
     * **/
    private final static int POLLING_RATE = 100;
    private AtomicLong seq = new AtomicLong(0l);

    /**
     * The Endpoint manager controls the Web Service instance during its whole
     * lifecycle.
     */
    private BinasEndpointManager endpointManager;

    /** Constructor receives a reference to the endpoint manager. */
    public BinasPortImpl(BinasEndpointManager endpointManager) {
        this.endpointManager = endpointManager;
    }

    // Main operations -------------------------------------------------------

    /**
     * 
     * @param coordinates
     * @param numberOfStations
     * @return
     *     returns java.util.List<org.binas.ws.StationView>
     */
    @Override
    public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {
    	if(numberOfStations == null || numberOfStations < 0 || coordinates == null)
    		return new ArrayList<StationView>(0);
    	
        BinasManager bm = BinasManager.getInstance();
        ArrayList<StationClient> stations = bm.findActiveStations();
        ArrayList<StationView> stationViews = new ArrayList<>();

        for(StationClient stationClient : stations) {
            stationViews.add(buildStationView(stationClient));
        }

        stationViews.sort(new CoordinatesComparator(coordinates));
        
        if(numberOfStations > stationViews.size()) 
        	return stationViews;
        else
        	return stationViews.subList(0, numberOfStations);
    }

    /**
     * 
     * @param stationId
     * @return
     *     returns org.binas.ws.StationView
     * @throws InvalidStation_Exception
     */
    @Override
    public StationView getInfoStation(String stationId) throws InvalidStation_Exception {
    	BinasManager bm = BinasManager.getInstance();

    	if (stationId == null || stationId.trim().equals("")) {
            throwInvalidStation("The station with ID '" + stationId + "' does not exist or could not be reached");
        }

    	StationClient stationClient = bm.lookupStation(stationId);
    	if(stationClient == null) {
    		throwInvalidStation("The station with ID '" + stationId + "' does not exist or could not be reached");
    	}
    	
    	return buildStationView(stationClient);
    }

    /**
     * 
     * @param email
     * @return
     *     returns int
     * @throws UserNotExists_Exception
     */
    @Override
    public int getCredit(String email) throws UserNotExists_Exception {
    	BinasManager bm = BinasManager.getInstance();
    	int credit = 0;
    	try {
    		credit = bm.getCredit(email);
    	} catch (UserNotExistsException e) {
    		throwUserNotExists("There is no user with email '" + email + "'.");
    	}
		return credit;
    }


	/**
     * 
     * @param email
     * @return
     *     returns org.binas.ws.UserView
     * @throws EmailExists_Exception
     * @throws InvalidEmail_Exception
     */

    public UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception {
        BinasManager bm = BinasManager.getInstance();
        UserView view = null;
        try {
            User user = bm.activateUser(email);
            quorumSetBalance(email, user.getCredit());
            synchronized(user) {
                view = buildUserView(user);
            }
        } catch (EmailExistsException e) {
            throwEmailExists("This email is already in use");
        } catch (InvalidEmailException e) {
            throwInvalidEmail("This email is invalid");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return view;
    }

	/**
     * 
     * @param email
     * @param stationId
     * @throws NoCredit_Exception
     * @throws InvalidStation_Exception
     * @throws NoBinaAvail_Exception
     * @throws UserNotExists_Exception
     * @throws AlreadyHasBina_Exception
     */
    @Override
    public void rentBina(String stationId, String email)
        throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception,
        NoCredit_Exception, UserNotExists_Exception {
        BinasManager bm = BinasManager.getInstance();

        try {
            bm.getBina(stationId, email);
        } catch (AlreadyHasBinaException e) {
            throwAlreadyHasBina("The user with email '" + email + "' is already renting a bina");
        } catch (InvalidStationException e) {
            throwInvalidStation("The station with ID '" + stationId + "' does not exist or could not be reached");
        } catch (NoBinaAvailException e) {
            throwNoBinaAvail("The station with ID '" + stationId + "' has no bina available (full)");
        } catch (NoCreditException e) {
            throwNoCredit("The user with email '" + email + "' does not have enough credit to rent a bina");
        } catch (UserNotExistsException e) {
            throwUserNotExists("There is no user with email '" + email + "'.");
        }
    }

    /**
     * 
     * @param email
     * @param stationId
     * @throws FullStation_Exception
     * @throws NoBinaRented_Exception
     * @throws InvalidStation_Exception
     * @throws UserNotExists_Exception
     */
    @Override
    public void returnBina(String stationId, String email)
        throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {

    	BinasManager bm = BinasManager.getInstance();

    	try {
			bm.returnBina(stationId, email);
		} catch (FullStationException e) {
			throwFullStation("The station with ID '" + stationId + "' has no slots available (full)");
		} catch (InvalidStationException e) {
    		throwInvalidStation("The station with ID '" + stationId + "' does not exist or could not be reached");
		} catch (NoBinaRentedException e) {
			throwNoBinaRented("The user with email '" + email + "' is not renting a bina");
		} catch (UserNotExistsException e) {
			throwUserNotExists("There is no user with email '" + email + "'.");
		}
    }

    /**
     * 
     * @param inputMessage
     * @return
     *     returns java.lang.String
     */
    @Override
    public String testPing(String inputMessage) {
    	BinasManager bm = BinasManager.getInstance();
    	ArrayList<StationClient> stationClients = bm.findActiveStations();
        
    	// Build a string with a message to return.
        StringBuilder builder = new StringBuilder();
        builder.append("Pinging all known stations with message: \n'").append(inputMessage).append("'\n");

    	for(StationClient stationClient : stationClients) {
    		builder.append(stationClient.testPing(inputMessage)).append("\n");
    	}

        builder.append("Pinging Complete!\n");

        return builder.toString();
    }

    @Override
    public void testClear() {
        BinasManager bm = BinasManager.getInstance();
        bm.testClear();
    }

    /**
     * 
     * @param returnPrize
     * @param x
     * @param y
     * @param stationId
     * @param capacity
     * @throws BadInit_Exception
     */
    @Override
    public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInit_Exception {
        BinasManager bm = BinasManager.getInstance();
        try {
        	bm.testInitStation(stationId, x, y, capacity, returnPrize);
        } catch (BadInitException e) {
        	throwBadInit("BinasPortImpl: " + e.getMessage());
        }
        
    }

    /**
     * 
     * @param userInitialPoints
     * @throws BadInit_Exception
     */
    @Override
    public void testInit(int userInitialPoints) throws BadInit_Exception {
        BinasManager bm = BinasManager.getInstance();
        try {
        	bm.testInit(userInitialPoints);
        } catch (BadInitException e) {
        	throwBadInit("Invalid initial parameters:\n" + e.getMessage());
        }
    }

    private void quorumSetBalance(String email, int points) throws ExecutionException, InterruptedException {
        long seq = this.seq.getAndIncrement();
        int i = 0;
        BinasManager bm = BinasManager.getInstance();
        List<Response<SetBalanceResponse>> pending = new ArrayList<>();
        for (StationClient station: bm.findActiveStations()) {
            System.out.println(String.format("CALL %d (%s) setBalanceAsync: %d, %s, %d", i++, station.getWsURL(), seq, email, points));
            pending.add(station.setBalanceAsync(buildUserReplica(seq, email, points)));
        }

        List<SetBalanceResponse> responses = new ArrayList<>();
        while(responses.size() < pending.size()/2 +1) {
            responses.clear();
            Thread.sleep(POLLING_RATE);
            System.out.println("-----Sleeping-----");
            i=0;
            for(Response<SetBalanceResponse> response : pending) {
                if (response.isDone()) {
                    System.out.println(String.format("RESPONSE %d setBalanceAsync: OK", i));
                    responses.add(response.get());
                }
                i++;
            }
        }
    }

    private int quorumGetBalance(String email) throws ExecutionException, InterruptedException {
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
        List<GetBalanceResponse> responses = new ArrayList<>();
        while(responses.size() < pending.size()/2 +1) {
            responses.clear();
            Thread.sleep(POLLING_RATE);
            System.out.println("-----Sleeping-----");
            i=0;
            for(Response<GetBalanceResponse> response : pending) {
                if (response.isDone()) {
                    System.out.println(String.format("RESPONSE %d setBalanceAsync: OK", i));
                    responses.add(response.get());
                }
                i++;
            }
        }

        //Find biggest sequence number
        long currentSeq;
        for(GetBalanceResponse response : responses) {
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


    // View helpers ----------------------------------------------------------

    /** Helper to convert user to a user view. */
    private UserView buildUserView(User user) {
    	UserView userView = new UserView();
    	userView.setEmail(user.getEmail());
    	userView.setHasBina(user.hasBina());
    	userView.setCredit(user.getCredit());
        return userView;
    }

    /** Helper to convert StationClient to a station view. */
    public StationView buildStationView(StationClient stationClient) {
        org.binas.station.ws.StationView svStation = stationClient.getInfo();
        StationView svBinas = new StationView();

        svBinas.setAvailableBinas(  svStation.getAvailableBinas());
        svBinas.setCapacity(        svStation.getCapacity());

        CoordinatesView cvBinas = new CoordinatesView();

        cvBinas.setX(               svStation.getCoordinate().getX());
        cvBinas.setY(               svStation.getCoordinate().getY());

        svBinas.setCoordinate(      cvBinas);
        svBinas.setFreeDocks(       svStation.getFreeDocks());
        svBinas.setId(              svStation.getId());
        svBinas.setTotalGets(       svStation.getTotalGets());
        svBinas.setTotalReturns(    svBinas.getTotalReturns());

        return svBinas;
    }

    public UserReplica buildUserReplica(long seq, String email, int points) {
        UserReplica replica = new UserReplica();
        replica.setEmail(email);
        replica.setPoints(points);
        replica.setSeq(seq);
        return replica;
    }

    // Exception helpers -----------------------------------------------------

    /** Helper to throw a new NoBinaAvail exception. */
    private void throwNoBinaAvail(final String message) throws
            NoBinaAvail_Exception {
        NoBinaAvail faultInfo = new NoBinaAvail();
        faultInfo.message = message;
        throw new NoBinaAvail_Exception(message, faultInfo);
    }

    /** Helper to throw a new BadInit exception. */
    private void throwBadInit(final String message) throws BadInit_Exception {
        BadInit faultInfo = new BadInit();
        faultInfo.message = message;
        throw new BadInit_Exception(message, faultInfo);
    }

    /** Helper to throw a new InvalidEmail exception. */
    private void throwInvalidEmail(String message) throws InvalidEmail_Exception {
    	InvalidEmail faultInfo = new InvalidEmail();
        faultInfo.message = message;
        throw new InvalidEmail_Exception(message, faultInfo);
	}

    /** Helper to throw a new InvalidStation exception. */
    private void throwInvalidStation(String message) throws InvalidStation_Exception {
    	InvalidStation faultInfo = new InvalidStation();
        faultInfo.message = message;
        throw new InvalidStation_Exception(message, faultInfo);
	}
    
    /** Helper to throw a new EmailExists exception. */
    private void throwEmailExists(String message) throws EmailExists_Exception {
    	EmailExists faultInfo = new EmailExists();
        faultInfo.message = message;
        throw new EmailExists_Exception(message, faultInfo);
	}
    
    /** Helper to throw a new UserNotExists exception. */
    private void throwUserNotExists(String message) throws UserNotExists_Exception {
		UserNotExists faultInfo = new UserNotExists();
		faultInfo.message = message;
		throw new UserNotExists_Exception(message, faultInfo);	
	}

    /** Helper to throw a new NoBinaRented exception. */
    private void throwNoBinaRented(String message) throws NoBinaRented_Exception {
		NoBinaRented faultInfo = new NoBinaRented();
		faultInfo.message = message;
		throw new NoBinaRented_Exception(message, faultInfo);
	}

    /** Helper to throw a new FullStation exception. */
    private void throwFullStation(String message) throws FullStation_Exception {
		FullStation faultInfo = new FullStation();
		faultInfo.message = message;
		throw new FullStation_Exception(message, faultInfo);
	}

    private void throwAlreadyHasBina(String message) throws AlreadyHasBina_Exception {
        AlreadyHasBina faultInfo = new AlreadyHasBina();
        faultInfo.message = message;
        throw new AlreadyHasBina_Exception(message, faultInfo);
    }

    private void throwNoCredit(String message) throws NoCredit_Exception {
        NoCredit faultInfo = new NoCredit();
        faultInfo.message = message;
        throw new NoCredit_Exception(message, faultInfo);
    }
}
