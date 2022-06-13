package com.example.notes.listeners;

import android.view.View;

import com.example.notes.entities.Note;

public interface CloudNotesListener {
    void onCloudNoteClicked(int position, View view);
}
