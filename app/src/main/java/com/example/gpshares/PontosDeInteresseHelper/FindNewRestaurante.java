package com.example.gpshares.PontosDeInteresseHelper;

public class FindNewRestaurante {
    public String avaliacao;
    public String comentario;
    public float latitude;
    public float longitude;

    public String getAvaliacao() {
        return avaliacao;
    }

    public FindNewRestaurante() {
    }

    public void setAvaliacao(String avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public FindNewRestaurante(String avaliacao, String comentario, float latitude, float longitude) {
        this.avaliacao = avaliacao;
        this.comentario = comentario;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
