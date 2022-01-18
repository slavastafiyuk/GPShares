package com.example.gpshares.FriendsHelper;

public class FindNewFriends {
    public String nomeInteiro;
    public String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public FindNewFriends(){

    }

    public FindNewFriends (String nomeInteiro){
        this.nomeInteiro=nomeInteiro;
    }

    public String getNomeInteiro() {
        return nomeInteiro;
    }

    public void setNomeInteiro(String nomeInteiro , String email) {
        this.nomeInteiro = nomeInteiro;
        this.email = email;
    }


}
