package com.example.notes.entities;

import com.google.gson.annotations.SerializedName;

public class UserInfo {
    @SerializedName("username")
    private String username;
    @SerializedName("user_pic")
    private String user_pic;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_pic() {
        return user_pic;
    }

    public void setUser_pic(String user_pic) {
        this.user_pic = user_pic;
    }
}
