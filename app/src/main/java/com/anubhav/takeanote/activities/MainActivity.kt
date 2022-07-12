package com.anubhav.takeanote.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.takeanote.R
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.rvadapter.NoteAdapter
import com.anubhav.takeanote.rvadapter.NoteItemClickInterface
import com.anubhav.takeanote.viewmodel.NoteViewModal
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Observer

class MainActivity : AppCompatActivity(), NoteItemClickInterface {

    // on below line we are creating a variable
    // for our recycler view, exit text, button and viewmodel.
    lateinit var viewModal: NoteViewModal
    lateinit var notesRV: RecyclerView
    lateinit var addFAB: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // on below line we are initializing
        // all our variables.
        notesRV = findViewById(R.id.notesRV)
        addFAB = findViewById(R.id.idFAB)

        // on below line we are setting layout
        // manager to our recycler view.
        notesRV.layoutManager = LinearLayoutManager(this)

        // on below line we are initializing our adapter class.
        val noteRVAdapter = NoteAdapter(this, this)

        // on below line we are setting
        // adapter to our recycler view.
        notesRV.adapter = noteRVAdapter

        // on below line we are
        // initializing our view modal.
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModal::class.java)

        // on below line we are calling all notes method
        // from our view modal class to observer the changes on list.
        viewModal.allNotes.observe(this) { list ->
            list?.let {
                // on below line we are updating our list.
                noteRVAdapter.submitList(it)
            }
        }

        addFAB.setOnClickListener {
            val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onNoteClick(note: Note) {
        // opening a new intent and passing a data to it.
        val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
        intent.putExtra("noteType", "Edit")
        intent.putExtra("noteData", note)
        startActivity(intent)
        finish()
    }

    override fun onNoteDeleteClick(note: Note) {
        viewModal.deleteNote(note)
        Toast.makeText(this, "${note.noteTitle} Deleted", Toast.LENGTH_LONG).show()
    }

}