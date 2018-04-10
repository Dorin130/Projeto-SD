package org.binas.domain;

import org.binas.station.ws.cli.StationClient;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;

import java.util.ArrayList;

public class BinasManager  {
    private static final String stationPrefix  = "";
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

	/*public static  synchronized ArrayList<StationClient> FindActiveStations() {

    }*/

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
