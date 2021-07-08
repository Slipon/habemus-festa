package com.example.habemusfesta.users;

public class Link {
    private String uid_user_1;
    private String uid_user_2;
    private String date;
    private String event_id;

    public Link(){}

    public Link(String uid_user_1, String uid_user_2, String event_id, String date){
        this.uid_user_1 = uid_user_1;
        this.uid_user_2 = uid_user_2;
        this.event_id = event_id;
        this.date = date;
    }

    public String getUid_user_1(){
        return this.uid_user_1;
    }
    public String getUid_user_2(){
        return this.uid_user_2;
    }
    public String getEvent_id(){ return this.event_id;}
    public String getDate(){
        return this.date;
    }
}
