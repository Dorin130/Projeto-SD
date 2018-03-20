package org.binas.station.ws.it;

import static org.junit.Assert.assertEquals;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.StationView;
import org.junit.Test;
import org.junit.After;




public class test_getInfoIT extends BaseIT {
    @Test
    public void success() throws BadInit_Exception {
        client.testInit(1, 2, 1, 5);
        StationView sv = client.getInfo();
        assertEquals(1, sv.getAvailableBinas());
        assertEquals(1, sv.getCoordinate().getX());
        assertEquals(2, sv.getCoordinate().getY());
        assertEquals(0, sv.getFreeDocks());
        assertEquals(0, sv.getTotalGets());
        assertEquals(0, sv.getTotalReturns());
    }
    
    @Test
    public void successAfterRepeatedGetBina()
    		throws BadInit_Exception, NoBinaAvail_Exception, NoSlotAvail_Exception {
    	
        client.testInit(0, 0, 50, 0);
        for(int i=0; i<40; i++) {
        	client.getBina();
        }
        for(int i=0; i<30; i++) {
        	client.returnBina();
        }
        StationView sv = client.getInfo();
        assertEquals(40, sv.getAvailableBinas());
        assertEquals(10, sv.getFreeDocks());
        assertEquals(40, sv.getTotalGets());
        assertEquals(30, sv.getTotalReturns());
    }
    
    @After
    public void tearDown() {
    	client.testClear();
    }
}
