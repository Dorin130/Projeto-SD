package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class test_listStationsIT extends BaseIT{

    @Before
    public void setup() throws InvalidEmail_Exception, EmailExists_Exception, BadInit_Exception {
		client.testInitStation(S1, S1X, S1Y, S1CAP, S1BONUS);
		client.testInitStation(S2, S2X, S2Y, S2CAP, S2BONUS);
		client.testInitStation(S3, S3X, S3Y, S3CAP, S3BONUS);
        client.activateUser(USER);
    }

    @Test
    public void listStationsOrderSuccess(){
        List<StationView> stationViews = client.listStations(3, CLOSE_TO_S1);
        Assert.assertEquals(3, stationViews.size());
        Assert.assertEquals(S1, stationViews.get(0).getId());
        Assert.assertEquals(S3, stationViews.get(1).getId());
        Assert.assertEquals(S2, stationViews.get(2).getId());


        stationViews = client.listStations(3, CLOSE_TO_S2);
        Assert.assertEquals(3, stationViews.size());
        Assert.assertEquals(S2, stationViews.get(0).getId());
        Assert.assertEquals(S3, stationViews.get(1).getId());
        Assert.assertEquals(S1, stationViews.get(2).getId());

        stationViews = client.listStations(3, CLOSE_TO_S3);
        Assert.assertEquals(3, stationViews.size());
        Assert.assertEquals(S3, stationViews.get(0).getId());
        Assert.assertEquals(S2, stationViews.get(1).getId());
        Assert.assertEquals(S1, stationViews.get(2).getId());
    }

    @Test
    public void listStationsMidpointSuccess(){
        List<StationView> stationViews = client.listStations(2, MIDPOINT);
        Assert.assertEquals(2, stationViews.size());
        ArrayList<String> results = new ArrayList<String>();
        for(StationView sv : stationViews) {
        	results.add(sv.getId());
        }
        Assert.assertTrue(results.contains(S2));
        Assert.assertTrue(results.contains(S3));
    }

    @Test
    public void listStationsMapPointSuccess(){
        List<StationView> stationViews = client.listStations(3, TESTPOINT);
        Assert.assertEquals(3, stationViews.size());
        ArrayList<String> results = new ArrayList<String>();
        for(StationView sv : stationViews) {
        	results.add(sv.getId());
        }
        Assert.assertTrue(results.contains(S1));
        Assert.assertTrue(results.contains(S2));
        Assert.assertTrue(results.contains(S3));
    }

    @Test
    public void listStationsEmptyListStations(){
        List<StationView> stationViews = client.listStations(0, TESTPOINT);
        Assert.assertEquals(0, stationViews.size());
    }

    @Test
    public void listStationsOneListStations(){
        List<StationView> stationViews = client.listStations(1, CLOSE_TO_S1);
        Assert.assertEquals(1, stationViews.size());
        Assert.assertEquals(S1, stationViews.get(0).getId());
    }

    @Test
    public void listStationsNegativeKInListStation(){
        List<StationView> stationViews = client.listStations(-1, CLOSE_TO_S1);
        Assert.assertEquals(0, stationViews.size());
    }
    
    @Test
    public void listStationsNullKInListStation(){
        List<StationView> stationViews = client.listStations(null, CLOSE_TO_S1);
        Assert.assertEquals(0, stationViews.size());
    }
    
    @Test
    public void listStationsNullCoordinatesInListStation(){
        List<StationView> stationViews = client.listStations(3, null);
        Assert.assertEquals(0, stationViews.size());
    }

    @After
    public void after() {
        client.testClear();
    }
}
