package com.example.gpshares;

import com.google.android.gms.maps.model.LatLng;

public class Estabelecimentos {
    public String avaliacao, comentario, nome, visibilidade;
    public double latitude;
    public double longitude;

    public Estabelecimentos(){

    }

    public Estabelecimentos(String nome, String avaliacao, String comentario, double latitude, double longitude, String visibilidade){
        this.nome = nome;
        this.avaliacao = avaliacao;
        this.comentario = comentario;
        this.latitude = latitude;
        this.longitude = longitude;
        this.visibilidade = visibilidade;
    }
}
