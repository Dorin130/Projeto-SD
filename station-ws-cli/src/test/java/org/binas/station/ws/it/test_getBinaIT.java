package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationView;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class test_getBinaIT extends BaseIT {
    @Before
    public void setup() {
        try {
            client.testInit(1,1, 1, 5);
        } catch (BadInit_Exception e1) {
            e1.printStackTrace();
        }
    }

    @Test(expected=NoBinaAvail_Exception.class)
    public void noBinaAvailable() throws NoBinaAvail_Exception{
            client.getBina();
            client.getBina();
    }

    @Test
    public void getBinaSuccess() throws NoBinaAvail_Exception{
            client.getBina();
            StationView sv = client.getInfo();
            Assert.assertEquals(0, sv.getAvailableBinas());
    }
    @Test
    public void getTwoBinas() throws NoBinaAvail_Exception{
        try {
            client.getBina();
            client.returnBina();
            client.getBina();
            StationView sv = client.getInfo();
            Assert.assertEquals(0, sv.getAvailableBinas());
        } catch (NoSlotAvail_Exception e) {
            e.printStackTrace();
        }

    }
    @After
    public void after() {
        client.testClear();
    }
}
