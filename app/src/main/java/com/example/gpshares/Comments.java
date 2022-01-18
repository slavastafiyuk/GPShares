package com.example.gpshares;

public class Comments {
    public String comment, date, nomeInteiro;

    public Comments(){

    }

    public Comments(String comment, String date, String nomeInteiro) {
        this.comment = comment;
        this.date = date;
        this.nomeInteiro = nomeInteiro;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNomeInteiro() {
        return nomeInteiro;
    }

    public void setNomeInteiro(String nomeInteiro) {
        this.nomeInteiro = nomeInteiro;
    }
}
