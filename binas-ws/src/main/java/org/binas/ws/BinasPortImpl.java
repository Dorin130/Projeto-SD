package org.binas.ws;

import org.binas.domain.BinasManager;
import org.binas.domain.User;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.binas.domain.exception.BadInitException;
import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.station.ws.cli.StationClient;

/**
 * This class implements the Web Service port type (interface). The annotations
 * below "map" the Java class to the WSDL definitions.
 */
// TODO
@WebService(endpointInterface = "org.binas.ws.BinasPortType",
        wsdlLocation = "binas.1_0.wsdl",
        name ="BinasWebService",
        portName = "BinasPort",
        targetNamespace="http://ws.binas.org/",
        serviceName = "BinasService"
)
public class BinasPortImpl implements BinasPortType {

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
		return null; //TODO
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
    	
		return null; //TODO
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
    		throwUserNotExists("There is no user with this email");
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
        	synchronized(user) {
            	view = buildUserView(user);
        	}
    	} catch (EmailExistsException e) {
    		throwEmailExists("This email is already in use");
    	} catch (InvalidEmailException e) {
    		throwInvalidEmail("This email is invalid");
    	}
    	return view;
    } //TODO ask teacher about this method

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
    	
    	//TODO
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
        throws FullStation_Exception, InvalidStation_Exception,
        NoBinaRented_Exception, UserNotExists_Exception {
    	//TODO
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
    	ArrayList<StationClient> stationClients = bm.findActiveStations(); //station client here or station view? ask teacher
        
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
    	//TODO
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
    	bm.testInit(userInitialPoints);
    }

    // View helpers ----------------------------------------------------------

    /** Helper to convert user to a user view. */
    private UserView buildUserView(User user) {
    	UserView userView = new UserView();
    	userView.setEmail(user.getEmail());
    	userView.setHasBina(user.isHasBina());
    	userView.setCredit(user.getCredit());
        return userView;
    }

    // Exception helpers -----------------------------------------------------

    /** Helper to throw a new NoBinaAvail exception. */
    private void throwNoBinaAvail(final String message) throws
            NoBinaAvail_Exception {
        NoBinaAvail faultInfo = new NoBinaAvail();
        faultInfo.message = message;
        throw new NoBinaAvail_Exception(message, faultInfo);
    }

    //** Helper to throw a new BadInit exception. */
    private void throwBadInit(final String message) throws BadInit_Exception {
        BadInit faultInfo = new BadInit();
        faultInfo.message = message;
        throw new BadInit_Exception(message, faultInfo);
    }

    private void throwInvalidEmail(String message) throws InvalidEmail_Exception {
    	InvalidEmail faultInfo = new InvalidEmail();
        faultInfo.message = message;
        throw new InvalidEmail_Exception(message, faultInfo);
	}
    
    private void throwEmailExists(String message) throws EmailExists_Exception {
    	EmailExists faultInfo = new EmailExists();
        faultInfo.message = message;
        throw new EmailExists_Exception(message, faultInfo);
	}
    
    private void throwUserNotExists(String message) throws UserNotExists_Exception {
		UserNotExists faultInfo = new UserNotExists();
		faultInfo.message = message;
		throw new UserNotExists_Exception(message, faultInfo);	
	}
}
