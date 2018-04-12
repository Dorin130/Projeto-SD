package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class test_getInfoStationIT extends BaseIT{
    private static final String USER = "joaozinho@tecnico.ulisboa.pt";
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
    public void getInfoStationSuccess() throws InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception,
            AlreadyHasBina_Exception, UserNotExists_Exception {
        StationView sv1 = client.getInfoStation(S1);
        Assert.assertEquals(6 ,sv1.getCapacity());
        Assert.assertEquals(6 ,sv1.getAvailableBinas());
        Assert.assertEquals(0 ,sv1.getFreeDocks());
        Assert.assertEquals(0 ,sv1.getTotalGets());
        Assert.assertEquals(0 ,sv1.getTotalReturns());
        Assert.assertEquals(22 ,sv1.getCoordinate().getX().intValue());
        Assert.assertEquals(7 ,sv1.getCoordinate().getY().intValue());
        Assert.assertEquals(S1 ,sv1.getId());

        StationView sv2 = client.getInfoStation(S1);
        Assert.assertEquals(12 ,sv2.getCapacity());
        Assert.assertEquals(12 ,sv2.getAvailableBinas());
        Assert.assertEquals(0 ,sv2.getFreeDocks());
        Assert.assertEquals(0 ,sv2.getTotalGets());
        Assert.assertEquals(0 ,sv2.getTotalReturns());
        Assert.assertEquals(80 ,sv2.getCoordinate().getX().intValue());
        Assert.assertEquals(20 ,sv2.getCoordinate().getX().intValue());
        Assert.assertEquals(S2 ,sv2.getId());

        client.rentBina(S3, USER);

        StationView sv3 = client.getInfoStation(S1);
        Assert.assertEquals(6 ,sv3.getCapacity());
        Assert.assertEquals(5 ,sv3.getAvailableBinas());
        Assert.assertEquals(1 ,sv3.getFreeDocks());
        Assert.assertEquals(1 ,sv3.getTotalGets());
        Assert.assertEquals(0 ,sv3.getTotalReturns());
        Assert.assertEquals(50 ,sv3.getCoordinate().getX().intValue());
        Assert.assertEquals(50 ,sv3.getCoordinate().getX().intValue());
        Assert.assertEquals(S3 ,sv3.getId());
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
