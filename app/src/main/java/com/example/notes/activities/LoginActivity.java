package com.example.notes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.example.notes.R;
import com.example.notes.entities.LoginResult;
import com.example.notes.Util.PostRequest;
import com.example.notes.Util.myJsonUtil;
import com.google.gson.Gson;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button submit;
    private Button regUser;
    private SharedPreferences sharedPreferences;

    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    String obj = (String) msg.obj;
                    Gson gson = new Gson();
                    LoginResult loginResult = gson.fromJson(obj,LoginResult.class);
                    if (loginResult.getStatus()!=0){
                        Toast.makeText(LoginActivity.this, loginResult.getMessage(), Toast.LENGTH_SHORT).show();
                    }else {
                        sharedPreferences =  getSharedPreferences("login",0);
                        sharedPreferences.edit().putString("token",loginResult.getToken()).commit();
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
        }
    }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.colorGray));
        BarUtils.setNavBarVisibility(this,false);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        submit = findViewById(R.id.submit);
        regUser = findViewById(R.id.regUser_navi);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(username.getText().toString()+" "+password.getText().toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PostRequest postRequest = new PostRequest();
                        try {
                            String result = postRequest.post("http://152.136.246.142:3007/api/login", username.getText().toString(), password.getText().toString());
                            if (myJsonUtil.isJson(result)){
                                Message message=mHandler.obtainMessage();
                                message.what=1;
                                message.obj=result;
                                mHandler.sendMessage(message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        regUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegUserActivity.class);
                startActivity(intent);
            }
        });

    }


}