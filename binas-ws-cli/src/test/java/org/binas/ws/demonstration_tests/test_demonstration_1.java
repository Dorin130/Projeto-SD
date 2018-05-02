package org.binas.ws.demonstration_tests;

import org.binas.ws.*;
import org.binas.ws.it.BaseIT;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class test_demonstration_1 extends BaseIT {

    @Before
    public void setup() throws BadInit_Exception {
        client.testInitStation(S1, S1X, S1Y, S1CAP, S1BONUS);
        client.testInitStation(S2, S2X, S2Y, S2CAP, S2BONUS);
        client.testInitStation(S3, S3X, S3Y, S3CAP, S3BONUS);
    }

    @Test
    public void persistenceSuccess() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception,
            NoBinaRented_Exception, EmailExists_Exception, InvalidEmail_Exception {

        System.out.println("Activating the users...");
        System.out.println("activateUser("+USER+")");
        client.activateUser(USER);
        System.out.println("activateUser("+USER2+")");
        client.activateUser(USER2);
        System.out.println("activateUser("+USER3+")");
        client.activateUser(USER3);

        System.out.println("Renting binas and checking credit...");
        System.out.println("rentBina("+S1 + ", " + USER + ")");
        client.rentBina(S1, USER);
        System.out.println("getCredit(" + USER + ")");
        Assert.assertEquals(INITIAL_POINTS - 1, client.getCredit(USER));

        System.out.println("rentBina("+S2 + ", " + USER2 + ")");
        client.rentBina(S2, USER2);
        System.out.println("getCredit(" + USER2 + ")");
        Assert.assertEquals(INITIAL_POINTS - 1, client.getCredit(USER2));

        System.out.println("rentBina("+S3 + ", " + USER3 + ")");
        client.rentBina(S3, USER3);
        System.out.println("getCredit(" + USER3 + ")");
        Assert.assertEquals(INITIAL_POINTS - 1, client.getCredit(USER3));

        System.out.println("Returning binas and checking credit...");
        System.out.println("returnBina("+S2 + ", " + USER + ")");
        client.returnBina(S2, USER);
        System.out.println("getCredit(" + USER + ")");
        Assert.assertEquals(INITIAL_POINTS - 1 + S2BONUS, client.getCredit(USER));

        System.out.println("returnBina("+S3 + ", " + USER2 + ")");
        client.returnBina(S3, USER2);
        System.out.println("getCredit(" + USER2 + ")");
        Assert.assertEquals(INITIAL_POINTS - 1 + S3BONUS, client.getCredit(USER2));

        System.out.println("returnBina("+S1 + ", " + USER3 + ")");
        client.returnBina(S1, USER3);
        System.out.println("getCredit(" + USER3 + ")");
        Assert.assertEquals(INITIAL_POINTS - 1 + S1BONUS, client.getCredit(USER3));
    }

    @After
    public void after() {
        client.testClear();
    }
}
