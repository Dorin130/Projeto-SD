package org.binas.domain;

public class User {

    private String email;
    private boolean hasBina;
    private int credit;

    public User(String email, boolean hasBina, int credit) {
        this.email = email;
        this.hasBina = hasBina;
        this.credit = credit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean hasBina() {
        return hasBina;
    }

    public void setHasBina(boolean hasBina) {
        this.hasBina = hasBina;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }
}
