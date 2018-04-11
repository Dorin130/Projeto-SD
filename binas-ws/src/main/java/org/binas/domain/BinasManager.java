package org.binas.domain;

import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class BinasManager  {
    private static final String stationPrefix  = "";
    private static final int INITIAL_CREDIT = 10;
    private static final String wsName  = "A17_Station";

    private Map<String, User> users = new HashMap<>();
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

	public static synchronized ArrayList<StationClient> FindActiveStations(String uddiURL) {
		ArrayList<StationClient> activeStationClients = new ArrayList<StationClient>(0);
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
		User newUser = new User(emailAddress, false, INITIAL_CREDIT);
		users.put(emailAddress, newUser);
		return newUser;
		
	}
	
	public boolean hasEmail(String email) {
		return users.containsKey(email);
	}
	
	public synchronized  void getBina(String stationId) {
		//TODO
	}

	public synchronized  void returnBina(String stationId) {
		//TODO
	}
	//ArrayList de station IDs

	//Criar class Coordinates ou adicionar dependencia no pom?
	public synchronized ArrayList<String> listStations(int k, CoordinatesView coordinates) {

		//TODO
        return null;
	}
	
	public StationView getStationView(int stationId, String uddiURL) {
		return null; //TODO
	}
	
	private StationClient getStationClient(int stationId, String uddiURL) {
		StationClient stationClient = null;
		try {
			stationClient = new StationClient(uddiURL, Integer.toString(stationId));
		} catch (StationClientException e) {
			e.printStackTrace();
		}
        return stationClient;
	}

	public synchronized int getCredit(String userEmail) throws UserNotExistsException {
		if(!hasEmail(userEmail)) throw new UserNotExistsException();
		return users.get(userEmail).getCredit();
	}
}
