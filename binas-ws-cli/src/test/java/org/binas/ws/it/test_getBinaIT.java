package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;


/**
 * Test suite
 */
public class test_getBinaIT extends BaseIT {
    private static final String USER = "joãozinho@tecnico.ulisboa.pt";
    private static final String S1 = "A17_TestStation1";
    private static final String S2 = "A17_TestStation2";
    private static final String S3 = "A17_TestStation3";
    private static final String S_EMPTY = "A17_TestEmptyTest";


    @Before
    public void setup() throws BadInit_Exception, InvalidEmail_Exception, EmailExists_Exception {
        client.testInitStation(S1,22, 7, 6, 2);
        client.testInitStation(S2,80, 20, 12, 1);
        client.testInitStation(S3,50, 50, 20, 0);
        client.testInitStation(S_EMPTY,1, 1, 0, 0);
        client.activateUser(USER);
    }


    @Test
    public void getBinaSuccess() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception {
		client.rentBina(S1, USER);
    }

    @Test(expected=AlreadyHasBina_Exception.class)
    public void getBinaFailAllreadyHasBina() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception {
        client.rentBina(S1, USER);
        client.rentBina(S2, USER);
    }

    @Test(expected=NoBinaAvail_Exception.class)
    public void getBinaFailNoBinaAvail() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception {
        client.rentBina(S_EMPTY, USER);
    }

    @Test(expected=NoCredit_Exception.class)
    public void getBinaFailNoCredit() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, BadInit_Exception,
            InvalidEmail_Exception, EmailExists_Exception {
        client.testInit(0);
        client.activateUser("nomoney@nomoney.com");
        client.rentBina(S1, "nomoney@nomoney.com");
    }

    @Test(expected=InvalidStation_Exception.class)
    public void getBinaFailUserNotExists() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception {
        client.rentBina("077008bf6e58c3cd21bb1f5107e5b214c9a89ef0", USER);
    }

    @After
    public void after() {
        client.testClear();
    }

}
