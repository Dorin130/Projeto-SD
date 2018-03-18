package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.StationView;
import org.junit.Assert;
import org.junit.Test;




public class test_getInfoIT extends BaseIT {

    @Test
    public void success() {
        try {
            client.testClear();
            client.testInit(1, 2, 1, 5);
            StationView sv = client.getInfo();
            Assert.assertEquals(1, sv.getAvailableBinas());
            Assert.assertEquals(1, sv.getCoordinate().getX());
            Assert.assertEquals(2, sv.getCoordinate().getY());
            Assert.assertEquals(0, sv.getFreeDocks());
            Assert.assertEquals(0, sv.getTotalGets());
            Assert.assertEquals(0, sv.getTotalReturns());

        } catch (BadInit_Exception e1) {
            e1.printStackTrace();
        }
    }
}
