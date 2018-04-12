package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class test_listStationsIT extends BaseIT{

    @Before
    public void setup() throws InvalidEmail_Exception, EmailExists_Exception {
        client.activateUser(USER);
    }

    @Test
    public void listStationsOneSuccess(){
        List<StationView> stationViews = client.listStations(1, CLOSE_TO_S1);

        Assert.assertEquals(S1, stationViews.get(0).getId());

    }

    @Test
    public void listStationsOrderSuccess(){
        List<StationView> stationViews = client.listStations(3, CLOSE_TO_S1);

        Assert.assertEquals(S1, stationViews.get(0).getId());
        Assert.assertEquals(S3, stationViews.get(1).getId());
        Assert.assertEquals(S2, stationViews.get(2).getId());

        stationViews = client.listStations(3, CLOSE_TO_S2);

        Assert.assertEquals(S2, stationViews.get(0).getId());
        Assert.assertEquals(S3, stationViews.get(1).getId());
        Assert.assertEquals(S1, stationViews.get(2).getId());

        stationViews = client.listStations(3, CLOSE_TO_S3);

        Assert.assertEquals(S3, stationViews.get(0).getId());
        Assert.assertEquals(S2, stationViews.get(1).getId());
        Assert.assertEquals(S1, stationViews.get(2).getId());
    }

    @Test
    public void listStationsMidpointSuccess(){
        client.listStations(3, MIDPOINT);
    }


    @After
    public void after() {
        client.testClear();
    }
}
