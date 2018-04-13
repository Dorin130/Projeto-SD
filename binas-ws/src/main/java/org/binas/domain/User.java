package org.binas.domain;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class User {

    private String email;
    private AtomicBoolean hasBina;
    private AtomicInteger credit;

    public User(String email, boolean hasBina, int credit) {
        this.email = email;
        this.hasBina.set(hasBina);
        this.credit.set(credit);
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
}
