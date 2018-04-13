package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;


/**
 * Test suite
 */
public class test_rentBinaIT extends BaseIT {

    @Before
    public void setup() throws InvalidEmail_Exception, EmailExists_Exception, BadInit_Exception {
        client.testInit(INITIAL_POINTS);
        client.activateUser(USER);
        client.activateUser(USER2);
        client.activateUser(USER3);
        client.testInitStation(S1,50, 22, 6, S1BONUS);
        client.testInitStation(S2,80, 20, 12, S2BONUS);
        client.testInitStation(S3,50, 50, 20, S3BONUS);
    }

    @Test
    public void rentBinaSuccess() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception {
		client.rentBina(S1, USER);
        client.rentBina(S2, USER2);
        client.rentBina(S2, USER3);
        Assert.assertEquals(INITIAL_POINTS-1 ,client.getCredit(USER));
        Assert.assertEquals(INITIAL_POINTS-1 ,client.getCredit(USER2));
        Assert.assertEquals(INITIAL_POINTS-1 ,client.getCredit(USER3));
        Assert.assertEquals(1, client.getInfoStation(S1).getTotalGets());
        Assert.assertEquals(2, client.getInfoStation(S2).getTotalGets());
    }

    @Test(expected=AlreadyHasBina_Exception.class)
    public void rentBinaFailAllreadyHasBina() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception {
        client.rentBina(S1, USER);
        client.rentBina(S2, USER);
    }

    @Test(expected=NoBinaAvail_Exception.class)
    public void rentBinaFailNoBinaAvail() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, BadInit_Exception {
        client.testInitStation(S1,22, 7, 0, 2);
        client.rentBina(S1, USER);
    }

    @Test(expected=NoCredit_Exception.class)
    public void rentBinaFailNoCredit() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, BadInit_Exception,
            InvalidEmail_Exception, EmailExists_Exception {
        client.testInit(0);
        client.activateUser("nomoney@nomoney.com");
        client.rentBina(S1, "nomoney@nomoney.com");
    }

    @Test(expected=InvalidStation_Exception.class)
    public void rentBinaFailStationNotExists() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception {
        client.rentBina("077008bf6e58c3cd21bb1f5107e5b214c9a89ef0", USER);
    }

    @Test(expected=UserNotExists_Exception.class)
    public void rentBinaFailUserNotExists() throws NoBinaAvail_Exception, NoCredit_Exception,
            InvalidStation_Exception, AlreadyHasBina_Exception, UserNotExists_Exception {
        client.rentBina(S1, "077008bf6e58c3cd21bb1f5107e5b214c9a89ef0");
    }

    @After
    public void after() {
        client.testClear();
    }

}
