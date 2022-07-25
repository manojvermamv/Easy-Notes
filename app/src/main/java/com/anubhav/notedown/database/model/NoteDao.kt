package com.anubhav.notedown.database.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemList(list: List<Note>)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notesTable WHERE taskId = :taskId")
    suspend fun delete(taskId: Int)

    @Query("DELETE FROM notesTable")
    suspend fun deleteAll()

    @Query("SELECT * FROM notesTable WHERE taskId = :taskID")
    suspend fun getNote(taskID: Int): Note

    @Query("SELECT * FROM notesTable ORDER BY timeStampDate DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notesTable WHERE isFavorite = 1 ORDER BY timeStampDate DESC")
    fun getAllFavoriteNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notesTable WHERE title LIKE :query AND description LIKE :query ORDER BY timeStampDate DESC")
    fun searchNotes(query: String): LiveData<List<Note>>

}