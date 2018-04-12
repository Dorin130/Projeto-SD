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
    }

    @Test
    public void returnBinaSuccess() throws AlreadyHasBina_Exception, NoBinaAvail_Exception,
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.rentBina(S1, USER);
        client.returnBina(S1, USER);
        client.rentBina(S2, USER);
        client.returnBina(S2, USER);
        client.rentBina(S3, USER);
        client.returnBina(S3, USER);
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
            NoCredit_Exception, InvalidStation_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception {
        client.rentBina(S2, USER);
        client.returnBina(S2,USER);
        Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER));
    }

    @After
    public void after() {
        client.testClear();
    }
}
