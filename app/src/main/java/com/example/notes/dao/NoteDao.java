package com.example.notes.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.notes.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :value || '%' or note_text LIKE '%' || :value || '%' or subtitle LIKE '%' || :value || '%'")
    List<Note> getSearchNotes(String value);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);
    @Delete
    void deleteNote(Note note);
}
