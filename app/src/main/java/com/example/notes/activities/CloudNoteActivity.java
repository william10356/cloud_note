package com.example.notes.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.notes.R;
import com.example.notes.Util.PostRequest;
import com.example.notes.Util.myJsonUtil;
import com.example.notes.adapters.CloudNoteAdapter;
import com.example.notes.entities.CloudNoteResult;
import com.example.notes.entities.Note;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

public class CloudNoteActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private ImageView cloud_note_imageBack;
    private CloudNoteAdapter MyCloud_noteAdapter;
    private RecyclerView MyCloud_recyclerView;
    private List<Note> MyCloudNote;
    private int noteClickedPosition = -1;

    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String obj = (String) msg.obj;
                    Gson gson = new Gson();
                    //云笔记获取结果: cloudNoteResult
                    CloudNoteResult cloudNoteResult = gson.fromJson(obj, CloudNoteResult.class);
                    if (cloudNoteResult.getStatus() == 0) {
                        //输出 post结果信息
                        Log.d("state", cloudNoteResult.getMessage());
                        MyCloudNote = cloudNoteResult.getNoteList();
                        Log.d("state", MyCloudNote.get(0).getImagePath());
                        // 将云笔记的list载入到MyCloud_noteAdapter中来更新样式
                        MyCloud_noteAdapter = new CloudNoteAdapter(CloudNoteActivity.this, MyCloudNote);

                        MyCloud_recyclerView.setAdapter(MyCloud_noteAdapter);
                    }
                    Toast.makeText(CloudNoteActivity.this, cloudNoteResult.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cload_note);
        MyCloud_recyclerView = findViewById(R.id.my_cloud_notesRecyclerView);
        cloud_note_imageBack = findViewById(R.id.cloud_note_imageBack);
        cloud_note_imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CloudNoteActivity.this, MainActivity.class));
                finish();
            }
        });
        MyCloud_recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        if (!TextUtils.isEmpty(sharedPreferences.getString("token", ""))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                    PostRequest postRequest = new PostRequest();
                    try {
                        String result= postRequest.post("http://152.136.246.142:3007/my/getupload",sharedPreferences.getString("token",""));
                        if (myJsonUtil.isJson(result)){
                            Log.d("result",result);
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


    }

}