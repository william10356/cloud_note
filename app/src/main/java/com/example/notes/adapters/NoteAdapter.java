package com.example.notes.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.R;
import com.example.notes.Util.MyBitmapUtil;
import com.example.notes.entities.Note;
import com.example.notes.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;
    private NotesListener notesListener;


    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public NoteAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, final int position) {
        holder.setNote(notes.get(position));

        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position),position);
            }
        });

    }


    @Override
    public int getItemCount() {
        return notes.size();
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