package com.netformular.multiplusmobileapp.model;

/**
 * Created by mac on 4/25/16.
 */
import java.io.Serializable;

public class ChatBox implements Serializable {
    String id, name, photo, lastMessage, timestamp;
    int unreadCount;

    public ChatBox() {
    }

    public ChatBox(String id, String name, String lastMessage, String timestamp, int unreadCount,String photo) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoto(String photo) { this.photo = photo; }

    public String getPhoto() { return this.photo; }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
