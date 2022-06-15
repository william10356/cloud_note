package com.example.notes.Util;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostRequest {
    public OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public  String  post(String url, String username, String password) throws IOException {
        client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("username",username).add("password",password).build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }

    }

    public String post_note(String url, String token, String note) throws IOException {
        client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, note);
        Request request = new Request.Builder()
                .addHeader("Authorization", token)
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }

    }
    public String post_note_for_page(String url, String token, String page) throws IOException {
        client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("page",page).build();
        Request request = new Request.Builder()
                .addHeader("Authorization", token)
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }

    }

    public String post_note_delete(String url, String token, String id) throws IOException {
        client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("id", id).build();
        Request request = new Request.Builder()
                .addHeader("Authorization", token)
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }

    }

    public String post(String url, String token) throws IOException {
        client = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("Authorization", token)
                .get()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    public String post(String url, String username, String password,String pic) throws IOException {
        client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("username",username).add("password",password).add("user_pic",pic).build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }

    }
}
