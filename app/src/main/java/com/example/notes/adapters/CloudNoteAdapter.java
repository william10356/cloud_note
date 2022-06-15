package com.example.notes.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.R;
import com.example.notes.Util.MyBitmapUtil;
import com.example.notes.Util.PostRequest;
import com.example.notes.Util.myJsonUtil;
import com.example.notes.activities.CloudNoteActivity;
import com.example.notes.activities.MainActivity;
import com.example.notes.database.NotesDatabase;
import com.example.notes.entities.Note;
import com.example.notes.listeners.CloudNotesListener;
import com.example.notes.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.IOException;
import java.util.List;

public class CloudNoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{
    private List<Note> notes;
    private NotesListener notesListener;
    private CloudNotesListener cloudNotesListener;
    private SharedPreferences sharedPreferences;
    private CloudNoteActivity cloudNoteActivity;
    public static final int CLOUD_RESULT_OK = -1;
    public static final int CLOUD_UPDATE_RESULT_OK=-2;

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public CloudNoteAdapter(CloudNoteActivity cloudNoteActivity, List<Note> notes, CloudNotesListener cloudNotesListener) {
        this.cloudNoteActivity = cloudNoteActivity;
        this.notes = notes;
        this.cloudNotesListener = cloudNotesListener;
    }
    public CloudNoteAdapter(CloudNoteActivity cloudNoteActivity,List<Note> notes) {
        this.cloudNoteActivity = cloudNoteActivity;
        this.notes = notes;
    }
    @NonNull
    @Override
    public NoteAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteAdapter.NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NoteViewHolder holder, final int position) {
        Note now_note = notes.get(position);
        holder.setNote(now_note);
        holder.layoutNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
            PopupMenu popupMenu = new PopupMenu(cloudNoteActivity,holder.itemView);
            popupMenu.inflate(R.menu.mycloudmenu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.recovery_note:
                            Log.d("cloud","下载");
                            class CheckNoteExitTask extends AsyncTask<Void,Void,List<Note>>{

                                @Override
                                protected List<Note> doInBackground(Void... voids) {
                                    return NotesDatabase.getNotesDatabase(cloudNoteActivity).noteDao().isAlreadyNoteExist(now_note.getId());
                                }

                                @Override
                                protected void onPostExecute(List<Note> notes) {
                                    super.onPostExecute(notes);
                                    if (notes.isEmpty()){
                                        //如果本机数据库没有云note则插入
                                        class RecoveryCloudNoteTask extends AsyncTask<Void,Void,Void>{

                                            @Override
                                            protected Void doInBackground(Void... voids) {
                                                NotesDatabase.getNotesDatabase(cloudNoteActivity).noteDao().insert(now_note);
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(Void unused) {
                                                super.onPostExecute(unused);
                                                Intent intent = new Intent();
                                                cloudNoteActivity.setResult(CLOUD_RESULT_OK, intent);
                                            }
                                        }
                                        new RecoveryCloudNoteTask().execute();
                                    }else {
                                        //如果数据库内有云note数据则覆盖
                                        class UpdateCloudNoteTask extends AsyncTask<Void,Void,Void>{


                                            @Override
                                            protected Void doInBackground(Void... voids) {
                                                NotesDatabase.getNotesDatabase(cloudNoteActivity).noteDao().updateNote(now_note);
                                                return null;

                                            }
                                            @Override
                                            protected void onPostExecute(Void unused) {
                                                super.onPostExecute(unused);
                                                Intent intent = new Intent();
                                                cloudNoteActivity.setResult(CLOUD_UPDATE_RESULT_OK, intent);
                                            }
                                        }
                                        new UpdateCloudNoteTask().execute();
                                    }
                                }
                            }
                            new CheckNoteExitTask().execute();

                            break;
                        case R.id.delete_note:
                            Log.d("cloud","删除");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    sharedPreferences = cloudNoteActivity.getSharedPreferences("login",cloudNoteActivity.MODE_PRIVATE);
                                    PostRequest postRequest = new PostRequest();
                                    try {
                                        String result= postRequest.post_note_delete("http://152.136.246.142:3007/my/deleteloadinfo",
                                                sharedPreferences.getString("token",""),
                                                now_note.getId());
                                        if (myJsonUtil.isJson(result)){
                                            Log.d("delete_result",result);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            notes.remove(position);
                            notifyDataSetChanged();
                    }
                    return false;
                }
            });
            popupMenu.show();
            return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
    public void updateList(List<Note> notes){
        this.notes=notes;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textSubtitle, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;


        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);

            imageNote = itemView.findViewById(R.id.imageNote);
        }

        //设置笔记卡片数据
        void setNote(Note note) {
            //标题
            textTitle.setText(note.getTitle());

            //判断副标题是否为空
            if (note.getSubtitle().trim().isEmpty()) {
                textSubtitle.setVisibility(View.GONE);
            }else {
                textSubtitle.setText(note.getSubtitle());
            }

            //保存时间
            textDateTime.setText(note.getDateTime());

            //卡片颜色
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor()!=null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            //卡片图片
            String temp_path = note.getImagePath();
            if (temp_path!=null) {
                if (!temp_path.startsWith("data")){
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(temp_path));
                }else {
                    temp_path =temp_path.replace("data:image/png;base64,","");
                    imageNote.setImageBitmap(MyBitmapUtil.base64ToBitmap(temp_path));
                }
                imageNote.setVisibility(View.VISIBLE);
            }else {
                imageNote.setVisibility(View.GONE);
            }
        }

    }
}
