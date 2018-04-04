package org.binas.station.ws.cli;

import org.binas.station.ws.StationView;

/** Client application. */
public class StationClientApp {

	public static void main(String[] args) throws Exception {
		// Check arguments.
		if (args.length != 1 && args.length != 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + StationClientApp.class.getName() + " wsURL OR uddiURL wsName");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		if (args.length == 1) {
			wsURL = args[0];
		} else if (args.length >= 2) {
			uddiURL = args[0];
			wsName = args[1];
		}

		System.out.println(StationClientApp.class.getSimpleName() + " running");

		// Create client.
		StationClient client = null;

		if (wsURL != null) {
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new StationClient(wsURL);
		} else if (uddiURL != null) {
			System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
			client = new StationClient(uddiURL, wsName);
		}

		// The following remote invocation is just a basic example.
		// The actual tests are made using JUnit.

		System.out.println("Invoke ping()...");
		String result = client.testPing("client");
		System.out.println("Result: ");
		System.out.println(result);

		System.out.println("Invoke getInfo()...");
		StationView sv = client.getInfo();
		System.out.println("Result: ");
		printStationView(sv);

		System.out.println("Invoke getBina()...");
		client.getBina();

		System.out.println("Invoke getInfo()...");
		sv = client.getInfo();
		System.out.println("Result: ");
		printStationView(sv);

		System.out.println("Invoke returnBina()...");
		int returnBina = client.returnBina();
		System.out.println("Result: ");
		System.out.println(returnBina);

		System.out.println("Invoke getInfo()...");
		sv = client.getInfo();
		System.out.println("Result: ");
		printStationView(sv);


	}

	private static void printStationView(StationView sv) {
		System.out.println("ID:\t\t" + sv.getId());
		System.out.println("AvailableBinas:\t\t" + sv.getAvailableBinas());
		System.out.println("Capacity:\t\t" + sv.getCapacity());
		System.out.println("Coordinate:\t\t" + sv.getCoordinate().getX() + ":" + sv.getCoordinate().getY());
		System.out.println("FreeDocks:\t\t" + sv.getFreeDocks());
		System.out.println("TotalGets:\t\t" + sv.getTotalGets());
		System.out.println("TotalReturns:\t\t" + sv.getTotalReturns());
	}

}
