package org.binas.domain;

import org.binas.ws.CoordinatesView;

import java.util.Comparator;

public class CoordinatesComparator implements Comparator<CoordinatesView> {
    private CoordinatesView clientPosition;

    public CoordinatesComparator(CoordinatesView clientPosition) {
        this.clientPosition = clientPosition;
    }

    private double calculateDistance(int stationX, int stationY) {
        return Math.sqrt(Math.pow(stationX-stationY,2) + Math.pow(clientPosition.getX()-clientPosition.getY(),2));
    }

    @Override
    public int compare(CoordinatesView o1, CoordinatesView o2) {
        return  (int)(calculateDistance(o1.getX(), o1.getY()) - calculateDistance(o2.getX(), o2.getY()));
    }
}
