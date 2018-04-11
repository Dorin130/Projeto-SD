package org.binas.domain;

import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.CoordinatesView;

import java.util.Comparator;

public class CoordinatesComparator implements Comparator<StationClient> {
    private CoordinatesView clientPosition;

    public CoordinatesComparator(CoordinatesView clientPosition) {
        this.clientPosition = clientPosition;
    }

    private double calculateDistance(int stationX, int stationY) {
        return Math.sqrt(Math.pow(stationX-stationY,2) + Math.pow(clientPosition.getX()-clientPosition.getY(),2));
    }

    @Override
    public int compare(StationClient s1, StationClient s2) {
        CoordinatesView c1 = s1.getInfo().getCoordinate();
        CoordinatesView c2 = s2.getInfo().getCoordinate();
        return  (int)(calculateDistance(c1.getX(), c1.getY()) - calculateDistance(c2.getX(), c2.getY()));
    }
}
