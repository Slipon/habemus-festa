package com.example.habemusfesta.events;

public class Event {
    private String data_inicial;
    private String data_final;
    private String descricao;
    private String localizacao;
    private String nome;
    private int likes;
    private int pontos;
    private String event_id;
    private String event_type;
    private String hora_inicial;
    private String hora_final;

    public Event(){}

    public Event(String event_id, String event_type, String nome, String descricao, String localizacao, int pontos, int likes, String data_inicial, String data_final, String hora_inicial, String hora_final){
        this.event_id = event_id;
        this.event_type = event_type;
        this.nome = nome;
        this.descricao = descricao;
        this.localizacao = localizacao;
        this.pontos = pontos;
        this.likes = likes;
        this.data_inicial = data_inicial;
        this.data_final = data_final;
        this.hora_inicial = hora_inicial;
        this.hora_final = hora_final;
    }

    public String getEvent_id(){ return this.event_id;}
    public String getEvent_type(){ return this.event_type;}
    public String getNome(){ return this.nome;}
    public String getDescricao(){ return this.descricao;}
    public String getLocalizacao(){ return this.localizacao;}
    public int getPontos(){ return this.pontos;}
    public int getLikes(){ return this.likes;}
    public void setLikes(int likes){this.likes = likes;}
    public String getData_inicial(){ return this.data_inicial;}
    public String getData_final(){ return this.data_final;}
    public String getHora_inicial(){ return this.hora_inicial;}
    public String getHora_final(){ return this.hora_final;}

}
