package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.cli.StationClient;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class User {

    private final String email;

    private boolean hasBina;
    private int credit; //"cached"

    private final AtomicLong seq;

    public User(String email, boolean hasBina, int credit) {
        this(email, hasBina, credit, 0);
    }

    public User(String email, boolean hasBina, int credit, long seq) {
        this.email = email;
        this.hasBina = hasBina;
        this.credit = credit;
        this.seq = new AtomicLong(seq);
    }

    public String getEmail() {
        return email;
    }

    public boolean hasBina() {
        return hasBina;
    }

    public void setHasBina(boolean hasBina) {
        this.hasBina = hasBina;
    } //for testing

    public int getCredit() {
        return credit;
    }

    public long getAndIncrementSeq() { return seq.getAndIncrement(); }

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

        this.credit--;
        bm.quorumSetBalance(getEmail(), this.credit, getAndIncrementSeq());

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

        this.credit += bonus;
        bm.quorumSetBalance(getEmail(), this.credit, getAndIncrementSeq());

        setHasBina(false);
    }
}
