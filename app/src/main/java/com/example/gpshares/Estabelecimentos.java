package com.example.gpshares;

import com.google.android.gms.maps.model.LatLng;

public class Estabelecimentos {
    public String avaliacao, comentario;
    public double latitude;
    public double longitude;

    public Estabelecimentos(){

    }

    public Estabelecimentos( String avaliacao, String comentario, double latitude, double longitude){
        this.avaliacao = avaliacao;
        this.comentario = comentario;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
