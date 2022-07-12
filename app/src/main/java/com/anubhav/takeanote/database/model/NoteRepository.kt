package com.anubhav.takeanote.database.model

import androidx.lifecycle.LiveData

class NoteRepository(private val noteDao: NoteDao) {

    // on below line we are creating a variable for our list
    // and we are getting all the notes from our DAO class.
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    // on below line we are creating an insert method
    // for adding the note to our database.
    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    // on below line we are creating a delete method
    // for deleting our note from database.
    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    suspend fun deleteAll() {
        noteDao.deleteAll()
    }

    // on below line we are creating a update method for
    // updating our note from database.
    suspend fun update(note: Note) {
        noteDao.update(note)
    }

}