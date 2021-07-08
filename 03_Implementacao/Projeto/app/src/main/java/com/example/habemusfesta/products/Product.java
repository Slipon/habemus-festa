package com.example.habemusfesta.products;

public class Product {
    private String evento_id;
    private String nome;
    private int pontos;
    private String image_url;

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Product(String evento_id, String nome, int pontos, String image_url ){
        this.evento_id = evento_id;
        this.nome = nome;
        this.pontos = pontos;
        this.image_url = image_url;
    }

    public String getEvento_id(){
        return this.evento_id;
    }

    public String getNome(){
        return this.nome;
    }

    public int getPontos(){
        return this.pontos;
    }

    public String getImage_url(){ return this.image_url;}
}
