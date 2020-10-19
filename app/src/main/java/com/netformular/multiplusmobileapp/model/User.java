package com.netformular.multiplusmobileapp.model;

import java.io.Serializable;

/**
 * Created by mac on 4/25/16.
 */
public class User implements Serializable {

    private String firstname,lastname,email,photo,userId;

    public User(){}

    public User(String userId,String firstname,String lastname,String email,String photo)
    {
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.photo = photo;
    }

    public void  setFirstname(String firstname)
    {
        this.firstname = firstname;
    }
    public String getFirstname()
    {
        return this.firstname;
    }
    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }
    public String getLastname()
    {
        return this.lastname;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }
    public String getEmail()
    {
        return this.email;
    }
    public void setPhoto(String photo)
    {
        this.photo = photo;
    }
    public String getPhoto()
    {
        return this.photo;
    }
    public void setUserId(String id)
    {
        this.userId = id;
    }
    public String getUserId()
    {
        return this.userId;
    }
}
