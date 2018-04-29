package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.cli.StationClient;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class User {

    private final String email;
    private final AtomicBoolean hasBina;
    private final AtomicInteger credit; //"cached"

    public User(String email, boolean hasBina, int credit) {
        this.email = email;
        this.hasBina = new AtomicBoolean(hasBina);
        this.credit = new AtomicInteger(credit);
    }

    public String getEmail() {
        return email;
    }

    public boolean hasBina() {
        return hasBina.get();
    }

    public void setHasBina(boolean hasBina) {
        this.hasBina.set(hasBina);
    } //for testing

    public int getCredit() {
        return credit.get();
    }

    //TODO: Ask teacher about double synchronized here (synchronized(credit) {synchronized(hasBina) {} } )
    synchronized void getBina(String stationId)  throws AlreadyHasBinaException,
            InvalidStationException, NoBinaAvailException, NoCreditException {

        if(getCredit() < 1) throw new NoCreditException();
        if(hasBina()) throw  new AlreadyHasBinaException();

        BinasManager bm = BinasManager.getInstance();
        StationClient stationClient = bm.lookupStation(stationId);
        if(stationClient == null) throw new InvalidStationException();

        try {
            stationClient.getBina();
        } catch (NoBinaAvail_Exception e) {
            throw new NoBinaAvailException("");
        }

        synchronized (credit) {
            try {
                bm.quorumSetBalance(getEmail(), getCredit() - 1);
            } catch (InterruptedException ie) {
                System.out.println("Thread was interrupted while executing quorumGetBalance. Terminating gracefully.");
                Thread.currentThread().interrupt(); //TODO: check this (we should respond to binas-cli)
            }
            this.credit.decrementAndGet();
        }
        setHasBina(true);
    }

    synchronized void returnBina(String stationId) throws FullStationException,
            InvalidStationException, NoBinaRentedException {

        if(!hasBina()) throw new NoBinaRentedException();

        BinasManager bm = BinasManager.getInstance();
        StationClient stationClient = bm.lookupStation(stationId);
        if(stationClient == null) throw new InvalidStationException();

        int bonus;
        try {
            bonus = stationClient.returnBina();
        } catch (NoSlotAvail_Exception e) {
            throw new FullStationException();
        }
        synchronized (credit) {
            try {
                bm.quorumSetBalance(getEmail(), getCredit() + bonus);
            } catch (InterruptedException ie) {
                System.out.println("Thread was interrupted while executing quorumGetBalance. Terminating gracefully.");
                Thread.currentThread().interrupt(); //TODO: check this (we should respond to binas-cli)
            }
            this.credit.addAndGet(bonus);
        }
        setHasBina(false);
    }
}
