package org.binas.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.binas.ws.CoordinatesView;
import org.binas.ws.cli.BinasClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;


/*
 * Base class of tests
 * Loads the properties in the file
 */
public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;

	protected static BinasClient client;

	protected static final String S1 = "A17_Station1";
	protected static final String S2 = "A17_Station2";
	protected static final String S3 = "A17_Station3";
	protected static final int INITIAL_POINTS = 10;
	protected static final String USER = "joaozinho@tecnico.ulisboa.pt";

	protected static final CoordinatesView CLOSE_TO_S1 = new CoordinatesView();
	protected static final CoordinatesView CLOSE_TO_S2 = new CoordinatesView();
	protected static final CoordinatesView CLOSE_TO_S3 = new CoordinatesView();
	protected static final CoordinatesView MIDPOINT = new CoordinatesView();

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		testProps = new Properties();
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Loaded test properties:");
			System.out.println(testProps);
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		final String uddiEnabled = testProps.getProperty("uddi.enabled");
		final String verboseEnabled = testProps.getProperty("verbose.enabled");

		final String uddiURL = testProps.getProperty("uddi.url");
		final String wsName = testProps.getProperty("ws.name");
		final String wsURL = testProps.getProperty("ws.url");

		if ("true".equalsIgnoreCase(uddiEnabled)) {
			client = new BinasClient(uddiURL, wsName);
		} else {
			client = new BinasClient(wsURL);
		}
		client.setVerbose("true".equalsIgnoreCase(verboseEnabled));

		client.testInit(INITIAL_POINTS);
		client.testInitStation(S1,22, 7, 6, 2);
		client.testInitStation(S2,80, 20, 12, 1);
		client.testInitStation(S3,50, 50, 20, 0);

		CLOSE_TO_S1.setX(22);
		CLOSE_TO_S1.setY(7);

		CLOSE_TO_S2.setX(80);
		CLOSE_TO_S2.setY(20);

		CLOSE_TO_S3.setX(50);
		CLOSE_TO_S3.setY(50);

		MIDPOINT.setX(50);
		MIDPOINT.setY(50);
	}

	@AfterClass
	public static void cleanup() {
	}

}
