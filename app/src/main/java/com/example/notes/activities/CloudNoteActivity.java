package com.example.notes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.notes.R;
import com.example.notes.Util.PostRequest;
import com.example.notes.Util.myJsonUtil;
import com.example.notes.adapters.NoteAdapter;
import com.example.notes.entities.CloudNoteResult;
import com.example.notes.entities.Note;
import com.example.notes.entities.UserImageResult;
import com.example.notes.listeners.NotesListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CloudNoteActivity extends AppCompatActivity{
    private SharedPreferences sharedPreferences;
    private String base64_pic;
    private NoteAdapter MyCloud_noteAdapter;
    private RecyclerView MyCloud_recyclerView;
    private List<Note> MyCloudNote;
    private int noteClickedPosition = -1;

    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    String obj = (String) msg.obj;
                    Gson gson = new Gson();
                    CloudNoteResult cloudNoteResult = gson.fromJson(obj, CloudNoteResult.class);
                    if (cloudNoteResult.getStatus() == 0) {
                        Log.d("state",cloudNoteResult.getMessage());
                        MyCloudNote = cloudNoteResult.getNoteList();
                        Log.d("state",MyCloudNote.get(0).getImagePath());
                        MyCloud_noteAdapter = new NoteAdapter(MyCloudNote, new NotesListener() {
                            @Override
                            public void onNoteClicked(Note note, int position) {
                                noteClickedPosition = position;
                                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                                intent.putExtra("isViewOrUpdate", true);
                                intent.putExtra("note", note);
                            }
                        });
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
        MyCloud_recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
        if (!TextUtils.isEmpty(sharedPreferences.getString("token",""))){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
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
//        Log.d("state", MyCloudNote.get(0).toString());

    }

}