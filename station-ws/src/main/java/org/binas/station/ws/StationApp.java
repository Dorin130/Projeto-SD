package org.binas.station.ws;

import org.binas.station.domain.Station;

/**
 * The application is where the service starts running. The program arguments
 * are processed here. Other configurations can also be done here.
 */
public class StationApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length != 3 && args.length != 4) {
			System.err.println("Argument(s) missing! " +Integer.toString(args.length));
			System.err.println("Usage: java " + StationApp.class.getName() + " wsName wsURL OR wsName wsURL uddiURL responseLag(ms)");
			return;
		}
		String wsName = args[0];
		String wsURL = args[1];
		String uddiURL;
		int lag = Integer.parseInt(args[3]);
		StationEndpointManager endpoint;

		if (args.length == 4) {
			uddiURL = args[2];
			endpoint = new StationEndpointManager(wsName, wsURL, uddiURL, lag);
		} else {
			endpoint = new StationEndpointManager(wsName, wsURL, lag);
		}

		Station.getInstance().setId(wsName);

		System.out.println(StationApp.class.getSimpleName() + " running");

		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}