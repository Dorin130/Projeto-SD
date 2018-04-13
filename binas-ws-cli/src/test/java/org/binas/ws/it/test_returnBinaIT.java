package org.binas.ws.it;

import org.binas.ws.NoBinaAvail_Exception;
import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class test_returnBinaIT extends BaseIT{

    @Before
    public void setup() throws InvalidEmail_Exception, EmailExists_Exception {
        client.activateUser(USER);
        client.activateUser(USER2);
        client.activateUser(USER3);
    }

    @Test
    public void returnBinaSuccess() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.rentBina(S1, USER);
        client.rentBina(S2, USER2);
        client.rentBina(S3, USER3);


        client.returnBina(S2, USER);
        Assert.assertEquals(INITIAL_POINTS - 1 + S2BONUS ,client.getCredit(USER));

        client.returnBina(S3, USER2);
        Assert.assertEquals(INITIAL_POINTS - 1 + S3BONUS ,client.getCredit(USER2));

        client.returnBina(S1, USER3);
        Assert.assertEquals(INITIAL_POINTS - 1 + S1BONUS ,client.getCredit(USER3));
    }

    @Test(expected=FullStation_Exception.class)
    public void returnBinaFailFullStation() throws NoBinaAvail_Exception, NoCredit_Exception,
            InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception, AlreadyHasBina_Exception {
        client.rentBina(S1, USER);
        client.returnBina(S2,USER);
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
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.rentBina(S2, USER);
        client.returnBina(S2,USER);
        Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER));
    }

    @Test
    public void returnBinaMultipleRents() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.rentBina(S1, USER);
        client.rentBina(S2, USER2);
        client.returnBina(S2, USER);
        client.rentBina(S3, USER);
        client.returnBina(S1, USER);
        client.returnBina(S3, USER2);
        Assert.assertEquals(INITIAL_POINTS -1 + S2BONUS -1 + S1BONUS, client.getCredit(USER));
        Assert.assertEquals(INITIAL_POINTS -1 + S3BONUS, client.getCredit(USER2));
    }

    @After
    public void after() {
        client.testClear();
    }
}
