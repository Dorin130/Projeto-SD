package org.binas.ws.cli;

import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.binas.ws.AlreadyHasBina_Exception;
import org.binas.ws.BadInit_Exception;
import org.binas.ws.BinasPortType;
import org.binas.ws.BinasService;
import org.binas.ws.CoordinatesView;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.FullStation_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.NoBinaAvail_Exception;
import org.binas.ws.NoBinaRented_Exception;
import org.binas.ws.NoCredit_Exception;
import org.binas.ws.StationView;
import org.binas.ws.UserNotExists_Exception;
import org.binas.ws.UserView;
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

/**
 * Client.
 *
 * Adds easier endpoint address configuration and UDDI lookup capability to the
 * PortType generated by wsimport.
 */
public class BinasClient implements BinasPortType {

	/** WS service */
	BinasService service = null;

	/** WS port (port type is the interface, port is the implementation) */
	BinasPortType port = null;

	/** UDDI server URL */
	private String uddiURL = null;

	/** WS name */
	private String wsName = null;

	/** WS endpoint address */
	private String wsURL = null; // default value is defined inside WSDL

	public String getWsURL() {
		return wsURL;
	}

	/** output option **/
	private boolean verbose = false;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL */
	public BinasClient(String wsURL) throws BinasClientException {
		this.wsURL = wsURL;
		createStub();
	}

	/** constructor with provided UDDI location and name */
	public BinasClient(String uddiURL, String wsName) throws BinasClientException {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
	}

	/** UDDI lookup */
	private void uddiLookup() throws BinasClientException {
		// TODO
	}

	/** Stub creation and configuration */
	private void createStub() {
		if (verbose)
			System.out.println("Creating stub ...");
		service = new BinasService();
		port = service.getBinasPort();

		if (wsURL != null) {
			if (verbose)
				System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		}
	}

	// remote invocation methods ----------------------------------------------

	@Override
	public UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception {
		return port.activateUser(email);
	}

	@Override
	public StationView getInfoStation(String stationId) throws InvalidStation_Exception {
		return port.getInfoStation(stationId);
	}

	@Override
	public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {
		return port.listStations(numberOfStations, coordinates);
	}

	@Override
	public void rentBina(String stationId, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception,
			NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception {
		port.rentBina(stationId, email);
	}

	@Override
	public void returnBina(String stationId, String email)
			throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
		port.returnBina(stationId, email);
	}

	@Override
	public int getCredit(String email) throws UserNotExists_Exception {
		return port.getCredit(email);
	}

	// test control operations ------------------------------------------------

	@Override
	public String testPing(String inputMessage) {
		return port.testPing(inputMessage);
	}

	@Override
	public void testClear() {
		port.testClear();
	}

	@Override
	public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize)
			throws BadInit_Exception {
		port.testInitStation(stationId, x, y, capacity, returnPrize);
	}

	@Override
	public void testInit(int userInitialPoints) throws BadInit_Exception {
		port.testInit(userInitialPoints);
	}

}