package com.example.habemusfesta.transactions;

public class Transaction {

    private String data;
    private String hora;
    private String pontos;
    private String uid_collab;
    private String uid_user;
    private String produto;

    public Transaction(){ }

    public Transaction(String data, String hora, String uid_collab, String uid_user, String produto, String pontos){
        this.data = data;
        this.hora = hora;
        this.uid_collab = uid_collab;
        this.uid_user = uid_user;
        this.produto = produto;
        this.pontos = pontos;
    }

    public String getData(){return this.data;}

    public String getHora(){
        return this.hora;
    }

    public String getUid_collab(){
        return this.uid_collab;
    }

    public String getUid_user(){
        return this.uid_user;
    }

    public String getProduto(){ return this.produto; }

    public String getPontos(){
        return this.pontos;
    }

}
