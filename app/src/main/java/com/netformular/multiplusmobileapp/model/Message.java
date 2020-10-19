package com.netformular.multiplusmobileapp.model;

import java.io.Serializable;

/**
 * Created by mac on 4/25/16.
 */
public class Message implements Serializable{

    private String id,message,sender,receiver,createdAt;
    private User user;

    public Message(){}

    public Message(String id,String sender,String receiver,String message)
    {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setUser(User user)
    {
        this.user = user;
    }
    public User getUser()
    {
        return this.user;
    }
}
