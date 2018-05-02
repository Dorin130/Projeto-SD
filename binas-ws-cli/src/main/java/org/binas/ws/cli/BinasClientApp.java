package org.binas.ws.cli;

import org.binas.ws.*;

public class BinasClientApp {
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

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + BinasClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
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

		System.out.println(BinasClientApp.class.getSimpleName() + " running");

        // Create client
        BinasClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new BinasClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new BinasClient(uddiURL, wsName);
        }

        System.out.println("---------------F1---------------");
        setup(client);
        activateUsers(client);
        run(client);
        teardown(client);
        System.out.println("Press ENTER to start F2");
        System.in.read();


        System.out.println("---------------F2---------------");
        setup(client);
        activateUsers(client);

        System.out.println("-----------Iteration 1----------");
        run(client);

        System.out.println("Please shut down (and restart) station 2/3 and press ENTER");
        System.in.read();
        System.in.read();
        System.out.println("-----------Iteration 2----------");
        run(client);

        System.out.println("Please restart binas and press ENTER");
        System.in.read();
        System.in.read();
        System.out.println("-----------Iteration 3----------");
        run(client);

        teardown(client);

	 }

    private static void setup(BinasClient client) throws BadInit_Exception {
        client.testInitStation(S1, S1X, S1Y, S1CAP, S1BONUS);
        client.testInitStation(S2, S2X, S2Y, S2CAP, S2BONUS);
        client.testInitStation(S3, S3X, S3Y, S3CAP, S3BONUS);
    }

    private static void activateUsers(BinasClient client) throws EmailExists_Exception, InvalidEmail_Exception {
        System.out.println("Activating the users...");
        System.out.println("activateUser(" + USER + ")");
        client.activateUser(USER);
        System.out.println("activateUser(" + USER2 + ")");
        client.activateUser(USER2);
        System.out.println("activateUser(" + USER3 + ")");
        client.activateUser(USER3);
    }

    private static void run(BinasClient client) throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception,
            NoBinaRented_Exception{
        System.out.println("Renting binas and checking credit...");
        System.out.println("rentBina("+S1 + ", " + USER + ")");
        client.rentBina(S1, USER);
        System.out.println("getCredit(" + USER + ") = " + Integer.toString(client.getCredit(USER)));

        System.out.println("rentBina("+S1 + ", " + USER2 + ")");
        client.rentBina(S1, USER2);
        System.out.println("getCredit(" + USER2 + ") = " + Integer.toString(client.getCredit(USER2)));

        System.out.println("rentBina("+S1 + ", " + USER3 + ")");
        client.rentBina(S1, USER3);
        System.out.println("getCredit(" + USER3 + ") = " + Integer.toString(client.getCredit(USER3)));

        System.out.println("Returning binas and checking credit...");
        System.out.println("returnBina("+S1 + ", " + USER + ")");
        client.returnBina(S1, USER);
        System.out.println("getCredit(" + USER + ") = " + Integer.toString(client.getCredit(USER)));

        System.out.println("returnBina("+S1 + ", " + USER2 + ")");
        client.returnBina(S1, USER2);
        System.out.println("getCredit(" + USER2 + ") = " + Integer.toString(client.getCredit(USER2)));

        System.out.println("returnBina("+S1 + ", " + USER3 + ")");
        client.returnBina(S1, USER3);
        System.out.println("getCredit(" + USER3 + ") = " + Integer.toString(client.getCredit(USER3)));
    }

    private static void teardown(BinasClient client) {
        client.testClear();
    }
}

