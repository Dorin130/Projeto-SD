package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class test_getInfoStationIT extends BaseIT{

    @Test
    public void getInfoStationSuccess() throws InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception,
            AlreadyHasBina_Exception, UserNotExists_Exception, EmailExists_Exception, InvalidEmail_Exception {
        StationView sv1 = client.getInfoStation(S1);
        Assert.assertEquals(6 ,sv1.getCapacity());
        Assert.assertEquals(6 ,sv1.getAvailableBinas());
        Assert.assertEquals(0 ,sv1.getFreeDocks());
        Assert.assertEquals(0 ,sv1.getTotalGets());
        Assert.assertEquals(0 ,sv1.getTotalReturns());
        Assert.assertEquals(50 ,sv1.getCoordinate().getX().intValue());
        Assert.assertEquals(22 ,sv1.getCoordinate().getY().intValue());
        Assert.assertEquals(S1 ,sv1.getId());

        StationView sv2 = client.getInfoStation(S2);
        Assert.assertEquals(12 ,sv2.getCapacity());
        Assert.assertEquals(12 ,sv2.getAvailableBinas());
        Assert.assertEquals(0 ,sv2.getFreeDocks());
        Assert.assertEquals(0 ,sv2.getTotalGets());
        Assert.assertEquals(0 ,sv2.getTotalReturns());
        Assert.assertEquals(80 ,sv2.getCoordinate().getX().intValue());
        Assert.assertEquals(20 ,sv2.getCoordinate().getY().intValue());
        Assert.assertEquals(S2 ,sv2.getId());

        client.activateUser(USER);
        client.rentBina(S3, USER);

        StationView sv3 = client.getInfoStation(S3);
        Assert.assertEquals(20 ,sv3.getCapacity());
        Assert.assertEquals(19 ,sv3.getAvailableBinas());
        Assert.assertEquals(1 ,sv3.getFreeDocks());
        Assert.assertEquals(1 ,sv3.getTotalGets());
        Assert.assertEquals(0 ,sv3.getTotalReturns());
        Assert.assertEquals(50 ,sv3.getCoordinate().getX().intValue());
        Assert.assertEquals(50 ,sv3.getCoordinate().getY().intValue());
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
