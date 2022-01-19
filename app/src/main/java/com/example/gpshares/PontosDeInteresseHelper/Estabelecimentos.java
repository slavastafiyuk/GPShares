package com.example.gpshares.PontosDeInteresseHelper;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

public class Estabelecimentos {
    public String avaliacao, comentario, nome, visibilidade;
    public double latitude;
    public double longitude;
    public String imagem;
    public int reports;

    public Estabelecimentos(){

    }

    public Estabelecimentos(String nome, String avaliacao, String comentario, double latitude, double longitude, String visibilidade, String imagem, int reports){
        this.nome = nome;
        this.avaliacao = avaliacao;
        this.comentario = comentario;
        this.latitude = latitude;
        this.longitude = longitude;
        this.visibilidade = visibilidade;
        this.imagem = imagem;
        this.reports = reports;
    }
}
