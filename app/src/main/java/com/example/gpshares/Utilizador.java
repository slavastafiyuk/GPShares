package com.example.gpshares;

public class Utilizador {
    public String nomeInteiro, email, identificador;
    public int AreaDeInteresse;

    public Utilizador(){
    }

    public Utilizador(String nomeInteiro, String email, String identificador, int AreaDeInteresse){
        this.nomeInteiro=nomeInteiro;
        this.email=email;
        this.identificador=identificador;
        this.AreaDeInteresse = AreaDeInteresse;
    }
}
