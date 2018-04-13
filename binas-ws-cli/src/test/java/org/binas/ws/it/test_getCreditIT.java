package org.binas.ws.it;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Test suite
 */
public class test_getCreditIT extends BaseIT {

    @Before
    public void setup() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser(USER);
    }

    @Test
    public void getCreditSuccess() throws UserNotExists_Exception {
        Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER));
    }

    @Test(expected=UserNotExists_Exception.class)
    public void getCreditUserNotExists() throws UserNotExists_Exception {
        client.getCredit("0a368cd5c6e31694f79de59c2173fb5efa239601");
    }

    @After
    public void after() {
        client.testClear();
    }

}
