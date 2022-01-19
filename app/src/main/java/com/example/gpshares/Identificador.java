package com.example.gpshares;

public class Identificador {
    private String identificador;
    private String userID;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Identificador(String userID, String identificador) {
        this.identificador = identificador;
        this.userID = userID;
    }
    public Identificador() {

    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
}
