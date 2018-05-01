package org.binas.ws.demonstration_tests;

import org.binas.ws.*;
import org.binas.ws.it.BaseIT;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class test_demonstration_2 extends BaseIT {

    @Before
    public void setup() throws BadInit_Exception {
        client.testInitStation(S1, S1X, S1Y, S1CAP, 1);
        client.testInitStation(S2, S2X, S2Y, S2CAP, 1);
        client.testInitStation(S3, S3X, S3Y, S3CAP, 1);
    }

    @Test
    public void persistenceSuccess() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception,
            NoBinaRented_Exception, EmailExists_Exception, InvalidEmail_Exception {

        System.out.println("Activating Users");

        client.activateUser(USER);
        client.activateUser(USER2);
        client.activateUser(USER3);

        for(int i=1; i<2; i++) {
            System.out.println(String.format("Iteration %d", i));

            client.rentBina(S1, USER);
            Assert.assertEquals(INITIAL_POINTS - 1, client.getCredit(USER));

            client.rentBina(S1, USER2);
            Assert.assertEquals(INITIAL_POINTS - 1, client.getCredit(USER2));

            client.rentBina(S1, USER3);
            Assert.assertEquals(INITIAL_POINTS - 1, client.getCredit(USER3));

            client.returnBina(S1, USER);
            Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER));

            client.returnBina(S1, USER2);
            Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER2));

            client.returnBina(S1, USER3);
            Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER3));

        }

    }

    @After
    public void after() {
        client.testClear();
    }
}
