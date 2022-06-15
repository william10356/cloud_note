package com.example.notes.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CloudNoteResult {
    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("pages")
    private int pages;
    @SerializedName("data")
    private List<Note> noteList;

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
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

    public List<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }
}
