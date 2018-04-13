package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test suite
 */
public class test_activateUserIT extends BaseIT {
    @Test
    public void activateUserSuccess() throws EmailExists_Exception, InvalidEmail_Exception, UserNotExists_Exception {
        //used getCredit to guarantee that the user exists because there's no other stub method that can verify directly that the user exists
        client.activateUser(USER);
        Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER));
        client.activateUser(USER2);
        Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER2));
        client.activateUser(USER3);
        Assert.assertEquals(INITIAL_POINTS, client.getCredit(USER3));
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
    public void activateUserInvalidEmailWithSpaces() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("test test@test.pt test");
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEmailWithoutSeparator() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("test.test.pt");
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEmailNoUserPart() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("test@");
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEmailNoDomainPart() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("@test");
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEmailNoUserAndNoDomainPart() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("@");
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEmailMultipleSeparators() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("test@test@test@test.pt");
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEmailEndsWithDot() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("test@pt.");
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEmailStartsWithDot() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser(".test@tecnico.pt");
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEndsWithDotInUserPart() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("test.@tecnico.pt");
    }

    @Test(expected=InvalidEmail_Exception.class)
    public void activateUserInvalidEmailStartsWithDotInDomainPart() throws EmailExists_Exception, InvalidEmail_Exception {
        client.activateUser("test.test@.");
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
