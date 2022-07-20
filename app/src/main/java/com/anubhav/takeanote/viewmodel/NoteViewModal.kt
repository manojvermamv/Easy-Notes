package com.anubhav.takeanote.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.anubhav.takeanote.database.AppDatabase
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.database.model.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NoteViewModal(application: Application) : AndroidViewModel(application) {

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NoteViewModal(application) as T
        }
    }

    // Public access - immutableLiveData
    var allNotes: LiveData<List<Note>>
    var allFavoriteNotes: LiveData<List<Note>>

    private val repository: NoteRepository

    // on below line we are initializing
    // our dao, repository and all notes
    init {
        val dao = AppDatabase.getDatabase(application).getNoteDao()
        repository = NoteRepository.getInstance(dao)
        allNotes = repository.allNotes
        allFavoriteNotes = repository.allFavoriteNotes
    }

    private fun getAllNotes(): List<Note> {
        return repository.allNotes.value ?: listOf()
    }

    fun updateNotes() = viewModelScope.launch(Dispatchers.IO) {
        allNotes = repository.allNotes
        allFavoriteNotes = repository.allFavoriteNotes
    }

    // on below line we are creating a new method for deleting a note. In this we are
    // calling a delete method from our repository to delete our note.
    fun deleteNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(note)
    }

    fun deleteNote(taskId: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(taskId)
    }

    fun deleteAllNote() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    // on below line we are creating a new method for updating a note. In this we are
    // calling a update method from our repository to update our note.
    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(note)
    }

    // on below line we are creating a new method for adding a new note to our database
    // we are calling a method from our repository to add a new note.
    fun addNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(note)
    }

    fun updateFavoriteNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        note.isFavorite = !note.isFavorite
        repository.update(note)
    }

    fun searchNotes(query: String?): List<Note> {
        val list: List<Note> = getAllNotes()
        val filterList = mutableListOf<Note>()

        for ((index, note) in list.withIndex()) {
            if (query == null || query.isEmpty()) {
                note.searchQuery = ""
                filterList.add(note)
            } else {
                val mQuery = query.trim()
                if (note.noteTitle.contains(mQuery)
                    || note.noteDescription.contains(mQuery)
                ) {
                    note.searchQuery = mQuery
                    filterList.add(note)
                }
            }
        }
        filterList.sortedBy { it.timeStamp }
        return filterList.toList()
    }

}