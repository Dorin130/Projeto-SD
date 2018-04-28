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
	protected static final int S1X = 22;
	protected static final int S1Y = 7;
	protected static final int S2X = 80;
	protected static final int S2Y = 20;
	protected static final int S3X = 50;
	protected static final int S3Y = 50;
	protected static final int S1CAP = 6;
	protected static final int S2CAP = 12;
	protected static final int S3CAP = 20;
	protected static final int S1BONUS = 2;
	protected static final int S2BONUS = 1;
	protected static final int S3BONUS = 0;
	protected static final int INITIAL_POINTS = 10;
	protected static final String USER = "LucasRafael@tecnico.ulisboa.pt";
	protected static final String USER2 = "Hugo.Guerreiro@tecnico.ulisboa.pt";
	protected static final String USER3 = "zucc@facebook";

	protected static final CoordinatesView CLOSE_TO_S1 = new CoordinatesView();
	protected static final CoordinatesView CLOSE_TO_S2 = new CoordinatesView();
	protected static final CoordinatesView CLOSE_TO_S3 = new CoordinatesView();
	protected static final CoordinatesView MIDPOINT = new CoordinatesView();
	protected static final CoordinatesView TESTPOINT = new CoordinatesView();

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
		client.testInitStation(S1, S1X, S1Y, S1CAP, S1BONUS);
		client.testInitStation(S2, S2X, S2Y, S2CAP, S2BONUS);
		client.testInitStation(S3, S3X, S3Y, S3CAP, S3BONUS);

		CLOSE_TO_S1.setX(S1X);
		CLOSE_TO_S1.setY(S1Y+1);

		CLOSE_TO_S2.setX(S2X);
		CLOSE_TO_S2.setY(S2Y+1);

		CLOSE_TO_S3.setX(S3X);
		CLOSE_TO_S3.setY(S3Y+1);

		MIDPOINT.setX(65); /* close midpoint for S2=(80,20) and S3=(50,50) */
		MIDPOINT.setY(35);

		TESTPOINT.setX(51); /* close center point for S1=(22,7), S2=(80,20) and S3=(50,50) */
		TESTPOINT.setY(26);
	}

	@AfterClass
	public static void cleanup() {
	}

}
