package com.example.notes.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserImageResult {
    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private UserInfo data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserInfo getData() {
        return data;
    }

    public void setData(UserInfo data) {
        this.data = data;
    }
}
