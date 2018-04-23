package com.example.fazlulhoque.iiucbususer;

/**
 * Created by Fazlul Hoque on 12/10/2017.
 */

public class AllUser {
    private String email,password,name,imageUrl,id;

    AllUser(){
    }

    public AllUser(String email, String password,String name,String imageUrl, String id) {
        this.email = email;
        this.password = password;
        this.name=name;
        this.imageUrl=imageUrl;
        this.id=id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
