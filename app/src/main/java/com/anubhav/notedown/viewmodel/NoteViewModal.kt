package com.anubhav.notedown.viewmodel

import android.app.Application
import android.app.Service
import android.content.Context
import androidx.lifecycle.*
import com.anubhav.notedown.database.AppDatabase
import com.anubhav.notedown.database.model.Note
import com.anubhav.notedown.database.model.NoteRepository
import kotlinx.coroutines.*

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

    fun getAllNotes(): List<Note> {
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

    suspend fun getNote(taskId: Int): Note {
        val note: Note = withContext(Dispatchers.IO) {
            repository.getNote(taskId)
        }
        return note
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

    fun searchNotes(query: String?): MutableList<Note> {
        val list: List<Note> = getAllNotes()
        val filterList = mutableListOf<Note>()

        for ((index, note) in list.withIndex()) {
            if (query == null || query.isEmpty()) {
                filterList.add(note)
            } else {
                val mQuery = query.trim()
                if (note.noteTitle.contains(mQuery) || note.noteDescription.contains(mQuery)) {
                    filterList.add(note)
                }
            }
        }
        //filterList.sortedBy { it.timeStampDate }
        return filterList
    }

    suspend fun getAllNotesAsStrings(list: MutableList<Int>): String {
        val result = viewModelScope.async {
            val sb = StringBuilder()
            list.forEach {
                val note = getNote(it)
                if (note.noteTitle.isNotEmpty()) {
                    sb.append(note.noteTitle).append("\n")
                }
                if (note.noteDescription.isNotEmpty()) {
                    sb.append(note.noteDescription)
                }
                sb.append("\n").append("\n")
            }
            sb.toString()
        }
        return result.await()
    }

    fun getAllNotesAsString(list: MutableList<Int>): String {
        return runBlocking(viewModelScope.coroutineContext) {
            val sb = StringBuilder()
            list.forEach {
                val note = getNote(it)
                if (note.noteTitle.isNotEmpty()) {
                    sb.append(note.noteTitle).append("\n")
                }
                if (note.noteDescription.isNotEmpty()) {
                    sb.append(note.noteDescription)
                }
                sb.append("\n").append("\n")
            }
            return@runBlocking sb.toString()
        }
    }

}