package com.example.habemusfesta.events;

public class EventsImages {
    private String image_url;
    private String nome;
    public EventsImages(){}
    public EventsImages(String image_url, String nome){
        this.image_url = image_url;
        this.nome = nome;
    }

    public String getImage_url(){ return this.image_url;}
    public String getNome(){ return this.nome;}
}
