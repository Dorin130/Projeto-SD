package org.binas.station.ws;

import org.binas.station.domain.Coordinates;
import org.binas.station.domain.Station;
import org.binas.station.domain.exception.BadInitException;
import org.binas.station.domain.exception.InvalidUserException;
import org.binas.station.domain.exception.NoBinaAvailException;
import org.binas.station.domain.exception.NoSlotAvailException;

import javax.jws.WebService;

/**
 * This class implements the Web Service port type (interface). The annotations
 * below "map" the Java class to the WSDL definitions.
 */
@WebService(endpointInterface = "org.binas.station.ws.StationPortType",
        wsdlLocation = "station.2_0.wsdl",
        name ="StationWebService",
        portName = "StationPort",
        targetNamespace="http://ws.station.binas.org/",
        serviceName = "StationService"
)
public class StationPortImpl implements StationPortType {

    /**
     * The Endpoint manager controls the Web Service instance during its whole
     * lifecycle.
     */
    private StationEndpointManager endpointManager;
    private int lag;

    private void sleep() {
        try {
            Thread.sleep(this.lag);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Constructor receives a reference to the endpoint manager. */
    public StationPortImpl(StationEndpointManager endpointManager) {
        this.endpointManager = endpointManager;
    }

    // Main operations -------------------------------------------------------

    /** Retrieve information about station. */
    public StationView getInfo() {
        sleep();
        Station s = Station.getInstance();
        synchronized(s) {
        	return buildStationView(s);
        }
    }

    /** Return a bike to the station. */
    @Override
    public int returnBina() throws NoSlotAvail_Exception {
        sleep();
        Station s = Station.getInstance();
        int result = -1;
        try {
            result = s.returnBina();
        } catch (NoSlotAvailException e) {
        	throwNoSlotAvail("There is no slot available.");
        }
        return result;
    }

    @Override
    public UserReplica getBalance(String email) throws InvalidUser_Exception {
        System.out.println(String.format("CALL getBalance (%s)", email));
        sleep();
        Station s = Station.getInstance();
        UserReplica user = null;
        try {
            user = s.getUser(email);
        } catch (InvalidUserException e) {
            System.out.println(String.format("RETURN getBalance InvalidUser_Exception"));
            throwInvalidUser("The user is invalid.");
        }
        System.out.println(String.format("RETURN getBalance (%d, %s, %d)", user.getSeq(), user.getEmail(), user.getPoints()));
        return user;
    }

    @Override
    public void setBalance(UserReplica user) {
        System.out.println(String.format("CALL setBalance (%d, %s, %d)", user.getSeq(), user.getEmail(), user.getPoints()));
        sleep();
        Station s = Station.getInstance();
        s.setUser(user);
        System.out.println(String.format("RETURN setBalance (void)"));
    }


    /** Take a bike from the station. */
    @Override
    public void getBina() throws NoBinaAvail_Exception {
        sleep();
        Station s = Station.getInstance();
        try {
            s.getBina();
        } catch (NoBinaAvailException e) {
        	throwNoBinaAvail("There is no Bina available.");
        }
    }



    // Test Control operations -----------------------------------------------

    /** Diagnostic operation to check if service is running. */
    @Override
    public String testPing(String inputMessage) {
        sleep();
        // If no input is received, return a default name.
        if (inputMessage == null || inputMessage.trim().length() == 0)
            inputMessage = "friend";

        // If the station does not have a name, return a default.
        String wsName = endpointManager.getWsName();
        if (wsName == null || wsName.trim().length() == 0)
            wsName = "Station";

        // Build a string with a message to return.
        StringBuilder builder = new StringBuilder();
        builder.append("Hello ").append(inputMessage);
        builder.append(" from ").append(wsName);
        return builder.toString();
    }

    /** Return all station variables to default values. */
    @Override
    public void testClear() {
        sleep();
        Station.getInstance().reset();
    }

    /** Set station variables with specific values. */
    @Override
    public void testInit(int x, int y, int capacity, int returnPrize) throws
            BadInit_Exception {
        sleep();
        try {
            Station.getInstance().init(x, y, capacity, returnPrize);
        } catch (BadInitException e) {
            throwBadInit("Invalid initialization values!");
        }
    }

    // View helpers ----------------------------------------------------------

    /** Helper to convert a domain station to a view. */
    private StationView buildStationView(Station station) {
        StationView view = new StationView();
        view.setId(station.getId());
        view.setCoordinate(buildCoordinatesView(station.getCoordinates()));
        view.setCapacity(station.getMaxCapacity());
        view.setTotalGets(station.getTotalGets());
        view.setTotalReturns(station.getTotalReturns());
        view.setFreeDocks(station.getFreeDocks());
        view.setAvailableBinas(station.getAvailableBinas());
        return view;
    }

    /** Helper to convert a domain coordinates to a view. */
    private CoordinatesView buildCoordinatesView(Coordinates coordinates) {
        CoordinatesView view = new CoordinatesView();
        view.setX(coordinates.getX());
        view.setY(coordinates.getY());
        return view;
    }

    // Exception helpers -----------------------------------------------------

    /** Helper to throw a new NoBinaAvail exception. */
    private void throwNoBinaAvail(final String message) throws
            NoBinaAvail_Exception {
        NoBinaAvail faultInfo = new NoBinaAvail();
        faultInfo.message = message;
        throw new NoBinaAvail_Exception(message, faultInfo);
    }

    /** Helper to throw a new NoSlotAvail exception. */
    private void throwNoSlotAvail(final String message) throws
            NoSlotAvail_Exception {
        NoSlotAvail faultInfo = new NoSlotAvail();
        faultInfo.message = message;
        throw new NoSlotAvail_Exception(message, faultInfo);
    }

    /** Helper to throw a new BadInit exception. */
    private void throwBadInit(final String message) throws BadInit_Exception {
        BadInit faultInfo = new BadInit();
        faultInfo.message = message;
        throw new BadInit_Exception(message, faultInfo);
    }
    /** Helper to throw a new InvalidUser exception. */
    private void throwInvalidUser(final String message) throws InvalidUser_Exception {
        InvalidUser faultInfo = new InvalidUser();
        faultInfo.message = message;
        throw new InvalidUser_Exception(message, faultInfo);
    }


    public void setLag(int lag) {
        this.lag = lag;
    }
}
