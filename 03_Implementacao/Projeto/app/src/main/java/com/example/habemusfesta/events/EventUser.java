package com.example.habemusfesta.events;

public class EventUser {
    private String user_id;
    private String evento_id;

    public EventUser(){ }

    public EventUser(String user_id, String evento_id){
        this.user_id = user_id;
        this.evento_id = evento_id;
    }

    public String getUser_id(){ return this.user_id;}
    public String getEvento_id(){ return this.evento_id;}
}
