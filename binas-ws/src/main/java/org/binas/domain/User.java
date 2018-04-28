package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.cli.StationClient;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class User {

    private String email;
    private AtomicBoolean hasBina;
    private AtomicInteger credit;

    public User(String email, boolean hasBina, int credit) {
        this.email = email;
        this.hasBina = new AtomicBoolean(hasBina);
        this.credit = new AtomicInteger(credit);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean hasBina() {
        return hasBina.get();
    }

    public void setHasBina(boolean hasBina) {
        this.hasBina.set(hasBina);
    }

    public int getCredit() {
        return credit.get();
    }

    public void setCredit(int credit) {
        this.credit.set(credit);
    }

    //TODO: Ask teacher about double synchronized here (synchronized(credit) {synchronized(hasBina) {} } )
    public synchronized void getBina(String stationId)  throws AlreadyHasBinaException,
        InvalidStationException, NoBinaAvailException, NoCreditException {

        if(getCredit() < 1) throw new NoCreditException();
        if(hasBina()) throw  new AlreadyHasBinaException();

        StationClient stationClient = BinasManager.lookupStation(stationId);
        if(stationClient == null) throw new InvalidStationException();

        try {
            stationClient.getBina();
        } catch (NoBinaAvail_Exception e) {
            throw new NoBinaAvailException("");
        }

        this.credit.decrementAndGet();
        setHasBina(true);
    }

    public synchronized void returnBina(String stationId)
        throws FullStationException, InvalidStationException, NoBinaRentedException {

        if(!hasBina()) throw new NoBinaRentedException();

        StationClient stationClient = BinasManager.lookupStation(stationId);
        if(stationClient == null) throw new InvalidStationException();

        int bonus;
        try {
            bonus = stationClient.returnBina();
        } catch (NoSlotAvail_Exception e) {
            throw new FullStationException();
        }

        this.credit.addAndGet(bonus);
        setHasBina(false);
    }
}
