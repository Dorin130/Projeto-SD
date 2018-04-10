package org.binas.domain;

import java.util.ArrayList;

public class BinasManager  {

	// Singleton -------------------------------------------------------------
	/*public StationView listStations(int numberOfStations, CoordinatesView coordinates) {}
	getCredit
	activateUser
	rentBina
	returnBina
	test_ping
	test_clear
	test_init_station
	test_init*/
	private BinasManager() {

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
	/*
	public synchronized ArrayList<String> listarEstacoes(int k, Coordinates coordinates) {
		//TODO
	}

	public synchronized double checkBalance(String userEmail) {
		//TODO
	}*/
}
