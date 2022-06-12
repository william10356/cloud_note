package com.example.notes.entities;

import com.google.gson.annotations.SerializedName;

public class LoginResult {

    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

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

}
