package org.binas.domain;

import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import java.util.ArrayList;
import java.util.Collection;

public class BinasManager  {
    private static final String wsName  = "A17_Station";
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
		UDDINaming uddiNaming;
		ArrayList<StationClient> activeStationClients = new ArrayList<StationClient>(0);
		try {
			uddiNaming = new UDDINaming(uddiURL);
			Collection<UDDIRecord> uddiRecords = uddiNaming.listRecords(wsName+"%");
			for(UDDIRecord record : uddiRecords) {
				activeStationClients.add(new StationClient(record.getOrgName(), record.getUrl()));
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
	public synchronized void ActivateUser(String emailAddress) {
		//TODO
		
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

	public synchronized double checkBalance(String userEmail) {
		return new Double(null);
		//TODO
	}
}
