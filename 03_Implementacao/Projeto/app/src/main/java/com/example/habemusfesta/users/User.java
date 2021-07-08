package com.example.habemusfesta.users;

public class User {

    private String username;
    private String nome;
    private String email;
    private String data_nascimento;
    private String user_type;
    private int normal_auth;
    private int google_auth;
    private int facebook_auth;
    private int pontos;
    //public

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String nome,  String email, String user_type, String data_nascimento, int normal_auth, int google_auth, int facebook_auth) {

        this.username = username;
        this.nome = nome;
        this.email = email;
        this.user_type = user_type;
        this.data_nascimento = data_nascimento;
        this.pontos = 0;
        this.normal_auth = normal_auth;
        this.google_auth = google_auth;
        this.facebook_auth = facebook_auth;

    }
    public String getUsername(){
        return this.username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public String getNome(){ return this.nome;}
    public void setNome(String nome){ this.nome = nome;}

    public String getUser_type(){
        return this.user_type;
    }
    public void setUser_type(String user_type){ this.user_type = user_type; }

    public String getData_nascimento(){ return this.data_nascimento;}
    public void setData_nascimento(String data_nascimento){ this.data_nascimento = data_nascimento;}

    public String getEmail(){
        return this.email;
    }
    public void setEmail(String email){ this.email = email; }

    public int getPontos(){ return this.pontos;}
    public void setPontos(int pontos){this.pontos = pontos;}

    public int getNormal_auth(){ return this.normal_auth; }
    public void setNormal_auth( int normal_auth){ this.normal_auth = normal_auth; }

    public int getGoogle_auth(){ return this.google_auth; }
    public void setGoogle_auth(int google_auth){ this.google_auth = google_auth; }

    public int getFacebook_auth(){ return this.facebook_auth; }
    public void setFacebook_auth(int facebook_auth){ this.facebook_auth = facebook_auth; }

}
