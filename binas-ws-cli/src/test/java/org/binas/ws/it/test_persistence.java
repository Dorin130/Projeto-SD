package org.binas.ws.it;

import org.binas.ws.*;
import org.binas.ws.it.BaseIT;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class test_persistence extends BaseIT {

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

        client.activateUser(USER);
        client.activateUser(USER2);
        client.activateUser(USER3);

        client.rentBina(S1, USER);
        Assert.assertEquals(INITIAL_POINTS - 1, client.getCredit(USER));

        client.rentBina(S2, USER2);
        Assert.assertEquals(INITIAL_POINTS - 1, client.getCredit(USER2));

        client.rentBina(S3, USER3);
        Assert.assertEquals(INITIAL_POINTS - 1, client.getCredit(USER3));

        client.returnBina(S2, USER);
        Assert.assertEquals(INITIAL_POINTS - 1 + S2BONUS, client.getCredit(USER));

        client.returnBina(S3, USER2);
        Assert.assertEquals(INITIAL_POINTS - 1 + S3BONUS, client.getCredit(USER2));

        client.returnBina(S1, USER3);
        Assert.assertEquals(INITIAL_POINTS - 1 + S1BONUS, client.getCredit(USER3));
    }

    @After
    public void after() {
        client.testClear();
    }
}
