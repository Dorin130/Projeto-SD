package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class test_getInfoStationIT extends BaseIT{

    @Before
    public void setup() throws BadInit_Exception {
		client.testInitStation(S1, S1X, S1Y, S1CAP, S1BONUS);
		client.testInitStation(S2, S2X, S2Y, S2CAP, S2BONUS);
		client.testInitStation(S3, S3X, S3Y, S3CAP, S3BONUS);
    }

    @Test
    public void getInfoStationSuccess() throws InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception,
            AlreadyHasBina_Exception, UserNotExists_Exception, EmailExists_Exception, InvalidEmail_Exception, BadInit_Exception {

        StationView sv1 = client.getInfoStation(S1);
        Assert.assertEquals(S1CAP, sv1.getCapacity());
        Assert.assertEquals(S1CAP, sv1.getAvailableBinas());
        Assert.assertEquals(0, sv1.getFreeDocks());
        Assert.assertEquals(0, sv1.getTotalGets());
        Assert.assertEquals(0, sv1.getTotalReturns());
        Assert.assertEquals(S1X, sv1.getCoordinate().getX().intValue());
        Assert.assertEquals(S1Y, sv1.getCoordinate().getY().intValue());
        Assert.assertEquals(S1, sv1.getId());

        StationView sv2 = client.getInfoStation(S2);
        Assert.assertEquals(S2CAP, sv2.getCapacity());
        Assert.assertEquals(S2CAP, sv2.getAvailableBinas());
        Assert.assertEquals(0, sv2.getFreeDocks());
        Assert.assertEquals(0, sv2.getTotalGets());
        Assert.assertEquals(0, sv2.getTotalReturns());
        Assert.assertEquals(S2X, sv2.getCoordinate().getX().intValue());
        Assert.assertEquals(S2Y, sv2.getCoordinate().getY().intValue());
        Assert.assertEquals(S2, sv2.getId());

        client.activateUser(USER);
        client.rentBina(S3, USER);

        StationView sv3 = client.getInfoStation(S3);
        Assert.assertEquals(S3CAP, sv3.getCapacity());
        Assert.assertEquals(S3CAP-1, sv3.getAvailableBinas());
        Assert.assertEquals(1 ,sv3.getFreeDocks());
        Assert.assertEquals(1 ,sv3.getTotalGets());
        Assert.assertEquals(0 ,sv3.getTotalReturns());
        Assert.assertEquals(S3X, sv3.getCoordinate().getX().intValue());
        Assert.assertEquals(S3Y, sv3.getCoordinate().getY().intValue());
        Assert.assertEquals(S3, sv3.getId());
    }

    @Test(expected=InvalidStation_Exception.class)
    public void getInfoStationInvalidStation() throws InvalidStation_Exception {
        client.getInfoStation("077008bf6e58c3cd21bb1f5107e5b214c9a89ef0");
    }


    @Test(expected=InvalidStation_Exception.class)
    public void getInfoStationEmptyStation() throws InvalidStation_Exception {
        client.getInfoStation("");
    }


    @Test(expected=InvalidStation_Exception.class)
    public void getInfoStationNullStation() throws InvalidStation_Exception {
        client.getInfoStation(null);
    }

    @After
    public void after() {
        client.testClear();
    }
}
