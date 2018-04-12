package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;


/**
 * Test suite
 */
public class test_rentBinaIT extends BaseIT {

    @Before
    public void setup() throws InvalidEmail_Exception, EmailExists_Exception {
        client.activateUser(USER);
    }

    @Test
    public void rentBinaSuccess() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception {
		client.rentBina(S1, USER);
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
    public void rentBinaFailUserNotExists() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception {
        client.rentBina("077008bf6e58c3cd21bb1f5107e5b214c9a89ef0", USER);
    }

    @After
    public void after() {
        client.testClear();
    }

}
