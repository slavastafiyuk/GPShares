package com.example.gpshares.PontosDeInteresseHelper;

public class Local {
    public String UserId;
    public String place;
    public String nomeDoLocal;

    public Local(String userId, String place, String nomeDoLocal) {
        UserId = userId;
        this.place = place;
        this.nomeDoLocal = nomeDoLocal;
    }

    public Local() {
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getNomeDoLocal() {
        return nomeDoLocal;
    }

    public void setNomeDoLocal(String nomeDoLocal) {
        this.nomeDoLocal = nomeDoLocal;
    }
}
