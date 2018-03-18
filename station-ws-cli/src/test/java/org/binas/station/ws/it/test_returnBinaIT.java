package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationView;
import org.junit.Assert;
import org.junit.Test;

public class test_returnBinaIT extends BaseIT{

    @Test(expected=NoSlotAvail_Exception.class)
    public void stationFull() throws NoSlotAvail_Exception {
        client.testClear();
        try {
            client.testInit(1,1, 1, 5);
            client.returnBina();
        } catch (BadInit_Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void returnBinaSuccess() throws NoSlotAvail_Exception{
        client.testClear();
        try {
            client.testInit(1,1, 1, 5);
            try {
                client.getBina();
                client.returnBina();
                StationView sv = client.getInfo();
                Assert.assertEquals(1, sv.getAvailableBinas());
            } catch (NoBinaAvail_Exception e) {
                e.printStackTrace();
            }
        } catch (BadInit_Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void returnTwoBinas() throws NoSlotAvail_Exception{
        client.testClear();
        try {
            client.testInit(1,1, 2, 5);
            try {
                client.getBina();
                client.getBina();
                client.returnBina();
                client.returnBina();
                StationView sv = client.getInfo();
                Assert.assertEquals(2, sv.getAvailableBinas());
            } catch (NoBinaAvail_Exception e) {
                e.printStackTrace();
            }
        } catch (BadInit_Exception e) {
            e.printStackTrace();
        }
    }
}
