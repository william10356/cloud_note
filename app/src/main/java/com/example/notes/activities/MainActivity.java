package com.example.notes.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.notes.R;
import com.example.notes.Util.MyBitmapUtil;
import com.example.notes.Util.PostRequest;
import com.example.notes.Util.myJsonUtil;
import com.example.notes.adapters.CloudNoteAdapter;
import com.example.notes.adapters.NoteAdapter;
import com.example.notes.database.NotesDatabase;
import com.example.notes.entities.Note;
import com.example.notes.entities.UserImageResult;
import com.example.notes.listeners.NotesListener;
import com.google.gson.Gson;
import com.loading.dialog.IOSLoadingDialog;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;

    public static final int REQUEST_CODE_UPDATE_NOTE = 2;

    private static final int REQUEST_CODE_SHOW_NOTE = 3;
    private static final int REQUEST_CLOUD_CODE_ADD_NOTE = 4;


    private RecyclerView notesRecyclerView;
    IOSLoadingDialog iosLoadingDialog = new IOSLoadingDialog().setOnTouchOutside(false);
    private List<Note> noteList;
    private NoteAdapter notesAdapter;
    private int noteClickedPosition = -1;
    private SearchView searchView;
    private RoundedImageView user_Image;
    private ImageView cloud_note;
    private SharedPreferences sharedPreferences;
    private String base64_pic;


    //获取post内信息来更新头像
    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    String obj = (String) msg.obj;
                    Gson gson = new Gson();
                    UserImageResult userInfoResult = gson.fromJson(obj, UserImageResult.class);
                    if (userInfoResult.getStatus() == 0) {
                        base64_pic = userInfoResult.getData().getUser_pic();
                        base64_pic = base64_pic.replace("data:image/png;base64,","");
                        Log.d("base",base64_pic);
                        Bitmap bitmap = MyBitmapUtil.base64ToBitmap(base64_pic);
                        user_Image.setImageBitmap(bitmap);
                    }
                    iosLoadingDialog.dismiss();
                    Toast.makeText(MainActivity.this, userInfoResult.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user_Image = findViewById(R.id.user_to_login);
        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        searchView = findViewById(R.id.inputSearch);
        cloud_note = findViewById(R.id.cloud_note);
        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        //判断本地有没有保存token如有则post无则不做任何任务
        if (!TextUtils.isEmpty(sharedPreferences.getString("token", ""))) {

            iosLoadingDialog.show(getFragmentManager(), "iosLoadingDialog");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                    Log.d("now",sharedPreferences.getString("token", ""));
                    PostRequest postRequest = new PostRequest();
                    try {
                        String result = postRequest.post("http://152.136.246.142:3007/my/userinfo", sharedPreferences.getString("token", ""));
                        if (result==null){
                            iosLoadingDialog.dismiss();
                        }
                        if (myJsonUtil.isJson(result)) {
                            Message message = mHandler.obtainMessage();
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


        //点击添加新笔记，并传REQUEST_CODE_ADD_NOTE至onActivityForResult
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startActivityForResult(
                      new Intent(getApplicationContext(), CreateNoteActivity.class),
                      REQUEST_CODE_ADD_NOTE
              );
            }
        });
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );



        noteList = new ArrayList<>();
        getNotes(REQUEST_CODE_SHOW_NOTE,false);
        notesAdapter = new NoteAdapter(noteList,this);
        notesRecyclerView.setAdapter(notesAdapter);




        user_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences =  getSharedPreferences("login",0);
                sharedPreferences.edit().remove("token").commit();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });

        //云盘跳转
        cloud_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, CloudNoteActivity.class),REQUEST_CLOUD_CODE_ADD_NOTE);
//                finish();
            }
        });

        //搜索功能


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                class GetSearchNoteTask extends AsyncTask<Void,Void,List<Note>>{
                    @Override
                    protected List<Note> doInBackground(Void... voids) {
                        return NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().getSearchNotes(newText);

                    }


                    @Override
                    protected void onPostExecute(List<Note> notes) {
                        super.onPostExecute(notes);
                        notesAdapter.setNotes(notes);
                        notesAdapter.notifyDataSetChanged();

                    }
                }
                new GetSearchNoteTask().execute();
                return true;
            }
        });


    }


    //笔记点击
    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);

    }
    //获取笔记
    private void getNotes(final int requestCode, boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetNoteTask extends AsyncTask<Void, Void, List<Note>> {
            @Override protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getNotesDatabase(getApplicationContext())
                        .noteDao()
                        .getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {//便签显示
                super.onPostExecute(notes);
                if (requestCode == REQUEST_CODE_SHOW_NOTE) {
                   noteList.addAll(notes);
                   notesAdapter.notifyDataSetChanged();
               }
                else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    //从数据库中获取最新的笔记，并更新列表
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }
                else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    //更新列表
                    //notes是当前数据库内数据并且按照时间降序
                    //notelist是进入到main才拿到的数据库数据。
                    noteList.remove(noteClickedPosition);

                    if (isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
                else if (requestCode == REQUEST_CLOUD_CODE_ADD_NOTE){
                    noteList.clear();
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }
                else if (requestCode ==REQUEST_CLOUD_CODE_ADD_NOTE){
                    noteList.clear();
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }
            }
        }
        new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        }
        else if (requestCode == REQUEST_CLOUD_CODE_ADD_NOTE && resultCode == CloudNoteAdapter.CLOUD_RESULT_OK){
            getNotes(REQUEST_CLOUD_CODE_ADD_NOTE,false);
        }
        else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
        else if (requestCode == REQUEST_CLOUD_CODE_ADD_NOTE && resultCode == CloudNoteAdapter.CLOUD_UPDATE_RESULT_OK){
            getNotes(REQUEST_CLOUD_CODE_ADD_NOTE,false);
        }
    }
}