package org.binas.ws.it;

import org.binas.ws.NoBinaAvail_Exception;
import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class test_returnBinaIT extends BaseIT{
    private static final String USER = "joãozinho@tecnico.ulisboa.pt";
    private static final String S1 = "A17_TestStation1";
    private static final String S2 = "A17_TestStation2";
    private static final String S3 = "A17_TestStation3";
    private static final int INITIAL_POINTS = 10;


    @Before
    public void setup() throws BadInit_Exception, InvalidEmail_Exception, EmailExists_Exception {
        client.testInit(INITIAL_POINTS);
        client.testInitStation(S1,22, 7, 6, 2);
        client.testInitStation(S2,80, 20, 12, 1);
        client.testInitStation(S3,50, 50, 20, 0);
        client.activateUser(USER);
    }


    @Test
    public void returnBinaSuccess() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.rentBina(S1, USER);
        client.returnBina(S1, USER);
        client.rentBina(S2, USER);
        client.returnBina(S2, USER);
        client.rentBina(S2, USER);
        client.returnBina(S2, USER);
        Assert.assertEquals(INITIAL_POINTS - 3 + 2 + 1 + 0 ,client.getCredit(USER));
    }

    @Test(expected=FullStation_Exception.class)
    public void returnBinaFailFullStation() throws NoBinaAvail_Exception, NoCredit_Exception,
            InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception, AlreadyHasBina_Exception {
        client.rentBina(S1, USER);
        client.returnBina(S2,USER);
        Assert.assertEquals(INITIAL_POINTS -1, client.getCredit(USER));
    }

    @Test
    public void returnBinaToStationWithNoReturnValue() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.rentBina(S3, USER);
        client.returnBina(S3,USER);
        Assert.assertEquals(INITIAL_POINTS -1, client.getCredit(USER));
    }

    @Test
    public void returnBinaToStationWithOneReturnValue() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, BadInit_Exception,
            InvalidEmail_Exception, EmailExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.rentBina(S2, USER);
        client.returnBina(S2,USER);
        Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER));
    }

    @After
    public void after() {
        client.testClear();
    }
}
