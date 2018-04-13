package org.binas.domain;

import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;

import java.util.Comparator;

public class CoordinatesComparator implements Comparator<StationView> {
    private CoordinatesView clientPosition;

    public CoordinatesComparator(CoordinatesView clientPosition) {
        this.clientPosition = clientPosition;
    }

    private double calculateSquareDistance(int stationX, int stationY) {
        return Math.pow(stationX-clientPosition.getX(),2) + Math.pow(stationY-clientPosition.getY(),2);
    }

    @Override
    public int compare(StationView s1, StationView s2) {
        CoordinatesView c1 = s1.getCoordinate();
        CoordinatesView c2 = s2.getCoordinate();
        return  (int)(calculateSquareDistance(c1.getX(), c1.getY()) - calculateSquareDistance(c2.getX(), c2.getY()));
    }
}
