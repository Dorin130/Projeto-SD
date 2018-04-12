package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Test;


/**
 * Test suite
 */
public class test_activateUserIT extends BaseIT {
    @Test
    public void activateUserSuccess() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser(USER);
    }

    @Test(expected=EmailExists_Exception.class)
    public void activateUserAlreadyExists() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser(USER);
        client.activateUser(USER);
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEmail() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("0a368cd5c6e31694f79de59c2173fb5efa239601");
    }


    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserEmpty() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("");
    }


    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserNull() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser(null);
    }


    @After
    public void after() {
        client.testClear();
    }

}
