package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class test_listStations extends BaseIT{
    private static final String USER = "joãozinho@tecnico.ulisboa.pt";
    private static final String S1 = "A17_TestStation1";
    private static final String S2 = "A17_TestStation2";
    private static final String S3 = "A17_TestStation3";
    private static final int INITIAL_POINTS = 10;
    private static final CoordinatesView CLOSE_TO_S1 = new CoordinatesView();
    private static final CoordinatesView CLOSE_TO_S2 = new CoordinatesView();
    private static final CoordinatesView CLOSE_TO_S3 = new CoordinatesView();
    private static final CoordinatesView MIDPOINT = new CoordinatesView();


    @Before
    public void setup() throws BadInit_Exception, InvalidEmail_Exception, EmailExists_Exception {
        client.testInit(INITIAL_POINTS);
        client.testInitStation(S1,22, 7, 6, 2);
        client.testInitStation(S2,80, 20, 12, 1);
        client.testInitStation(S3,50, 50, 20, 0);
        client.activateUser(USER);

        CLOSE_TO_S1.setX(22);
        CLOSE_TO_S1.setY(7);

        CLOSE_TO_S2.setX(80);
        CLOSE_TO_S2.setY(20);

        CLOSE_TO_S3.setX(50);
        CLOSE_TO_S3.setY(50);

        MIDPOINT.setX(50);
        MIDPOINT.setY(50);
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
