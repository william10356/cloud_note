package com.example.notes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.example.notes.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
    private TextView button_navi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //设置下布局向上移动状态栏的高度
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.colorGray));
        //设置隐藏虚拟按键
        BarUtils.setNavBarVisibility(this,false);



        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toMain();
            }
        },3*1000);
        button_navi = findViewById(R.id.skip_to);
        button_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toMain();
            }
        });
    }

    private void toMain() {
        Intent intent = new Intent();
        intent.setClass(SplashActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }


}