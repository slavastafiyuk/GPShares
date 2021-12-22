package com.example.gpshares;

import com.google.android.gms.maps.model.LatLng;

public class Estabelecimentos {
    public String avaliacao, comentario;
    public LatLng localizacao;

    public Estabelecimentos( String avaliacao, String comentario, LatLng localizacao){
        this.avaliacao = avaliacao;
        this.comentario = comentario;
        this.localizacao = localizacao;
    }
}
