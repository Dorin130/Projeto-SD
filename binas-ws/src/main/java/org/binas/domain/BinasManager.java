package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.station.ws.CoordinatesView;
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

	public synchronized ArrayList<StationClient> findActiveStations(/*String uddiURL*/) {
		ArrayList<StationClient> activeStationClients = new ArrayList<StationClient>();
		UDDINaming uddiNaming;
		try {
			uddiNaming = new UDDINaming(uddiURL);
			Collection<String> wsURLs = uddiNaming.list(wsName+"%");
			for(String wsURL : wsURLs) {
				activeStationClients.add(new StationClient(wsURL));
			}
		} catch (UDDINamingException e) {
			e.printStackTrace();
		} catch (StationClientException e) {
			e.printStackTrace();
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

	public synchronized void getBina(String stationId, String userEmail) throws UserNotExistsException {
			if(!hasEmail(userEmail)) throw new UserNotExistsException();
			StationClient stationClient = getStationClient(stationId);
			//if(stationClient == null) throw InvalidStationException;
			User user = getUser(userEmail);
			if(user.getCredit() >= 1 && !user.hasBina()) {
				try {
					stationClient.getBina();
				} catch (NoBinaAvail_Exception e) {
					e.printStackTrace();
				}
			}
	}

	public synchronized  void returnBina(String stationId, String userEmail) {
		//TODO
	}
	//ArrayList de station IDs
	//Criar class Coordinates ou (adicionar dependencia no pom NAO)
	public synchronized ArrayList<StationClient> listStations(int k, CoordinatesView coordinates, String uddiURL) {
        ArrayList<StationClient> stations = this.findActiveStations();
        stations.sort(new CoordinatesComparator(coordinates));
        if(k > stations.size()) {
            return stations;
        }
        for(int i=stations.size(); i > stations.size() - k; i--  ) {
            stations.remove(i);
        }
        stations.trimToSize();
        return stations;

	}
	
	public StationView getStationView(String stationId, String uddiURL) {
		return null; //TODO
	}
	
	private StationClient getStationClient(String stationId) {
		StationClient stationClient = null;
		try {
			stationClient = new StationClient(uddiURL, stationId);
		} catch (StationClientException e) {
			e.printStackTrace();
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

	// test mthods -------

	public void testInit(int userInitialPoints) {
		initialPoints = userInitialPoints;
	}


	public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInitException {
		StationClient station = getStationClient(stationId);
		//station.testInit(x, y, capacity, returnPrize);

	}

}
