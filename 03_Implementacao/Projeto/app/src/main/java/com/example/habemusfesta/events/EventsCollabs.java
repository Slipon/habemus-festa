package com.example.habemusfesta.events;

public class EventsCollabs {

    private String evento_id;
    private String collab_id;
    private String nome;

    public EventsCollabs(){}
    public EventsCollabs(String evento_id, String collab_id, String nome){
        this.evento_id = evento_id;
        this.collab_id = collab_id;
        this.nome = nome;
    }

    public String getEvento_id(){ return this.evento_id;}
    public String getCollab_id(){ return this.collab_id;}
    public String getNome(){ return this.nome;}
}
