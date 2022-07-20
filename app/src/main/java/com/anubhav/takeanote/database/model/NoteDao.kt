package com.anubhav.takeanote.database.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notesTable WHERE taskId = :taskId")
    suspend fun delete(taskId: Int)

    @Query("DELETE FROM notesTable")
    suspend fun deleteAll()

    @Query("SELECT * FROM notesTable ORDER BY taskId DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notesTable WHERE isFavorite = 1 ORDER BY taskId DESC")
    fun getAllFavoriteNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notesTable WHERE title LIKE '%' || :query || '%' AND description LIKE '%' || :query || '%'")
    fun searchNotes(query: String): LiveData<List<Note>>

}