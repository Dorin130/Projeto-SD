package org.binas.ws;

import org.binas.domain.BinasManager;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class implements the Web Service port type (interface). The annotations
 * below "map" the Java class to the WSDL definitions.
 */
// TODO
@WebService(endpointInterface = "org.binas.station.ws.StationPortType",
        wsdlLocation = "station.1_0.wsdl",
        name ="StationWebService",
        portName = "StationPort",
        targetNamespace="http://ws.station.binas.org/",
        serviceName = "StationService"
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
		return 0; //TODO
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
		return null; //TODO
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
        throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
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
        // If no input is received, return a default name.
        if (inputMessage == null || inputMessage.trim().length() == 0)
            inputMessage = "friend";

        // If the station does not have a name, return a default.
        String wsName = endpointManager.getWsName();
        if (wsName == null || wsName.trim().length() == 0)
            wsName = "Binas";

        // Build a string with a message to return.
        StringBuilder builder = new StringBuilder();
        builder.append("Hello ").append(inputMessage);
        builder.append(" from ").append(wsName);
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
    	//TODO
    }

    /**
     * 
     * @param userInitialPoints
     * @throws BadInit_Exception
     */
    @Override
    public void testInit(int userInitialPoints) throws BadInit_Exception {
    	//TODO
    }

    // View helpers ----------------------------------------------------------

    /** Helper to convert a domain coordinates to a view. *//*
    private UserView buildUserView(Coordinates coordinates) {
        CoordinatesView view = new CoordinatesView();
        view.setX(coordinates.getX());
        view.setY(coordinates.getY());
        return view;
    }*/

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

}
