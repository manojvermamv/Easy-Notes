package com.anubhav.takeanote.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.takeanote.R
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.databinding.ActivityAddEditNoteBinding
import com.anubhav.takeanote.utils.DateTimeUtils
import com.anubhav.takeanote.utils.Global
import com.anubhav.takeanote.utils.GlobalData
import com.anubhav.takeanote.viewmodel.NoteViewModal

class AddEditNoteActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddEditNoteBinding
    var isUpdateNote: Boolean = false

    // on below line we are creating variable for
    // viewmodal and and integer for our note id.
    lateinit var viewModal: NoteViewModal
    lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_edit_note)
        GlobalData.setStatusBarBackgroundColor(this, R.color.white)
        FontUtils.setFont(this, binding.root as ViewGroup)

        // on below line we are getting data passed via an intent.
        isUpdateNote = intent.getBooleanExtra("updateNote", false)
        if (isUpdateNote) {
            note = intent.getSerializableExtra("noteData") as Note
        }

        onCreateActionBar()
        onCreateApp()
    }

    private fun onCreateActionBar() {
        binding.layActionbar.actionBarTitle = getString(R.string.app_name)
        binding.layActionbar.menuVisible = true

        binding.layActionbar.imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }

    private fun onCreateApp() {
        if (isUpdateNote) {
            binding.saveBtn.text = "Update"
            binding.edtNoteName.setText(note.noteTitle)
            binding.edNoteDesc.setText(note.noteDescription)
        } else {
            binding.saveBtn.text = "Save"
        }

        // on below line we are initialing our view modal.
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModal::class.java)

        binding.saveBtn.setOnClickListener {
            saveNote()
            finish()
        }
    }

    override fun onBackPressed() {
        saveNote()
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        binding.edNoteDesc.isFocusable = true
        showKeyboard()
    }

    private fun saveNote() {
        val noteTitle = binding.edtNoteName.text.toString().trim()
        val noteDescription = binding.edNoteDesc.text.toString().trim()

        if (noteDescription.isNotEmpty()) {
            val currentTime: String = DateTimeUtils.getCurrentTime()
            if (isUpdateNote) {
                val updatedNote = Note(noteTitle, noteDescription, currentTime)
                updatedNote.taskId = note.taskId
                viewModal.updateNote(updatedNote)
            } else {
                viewModal.addNote(Note(noteTitle, noteDescription, currentTime))
            }
        }

        if (isUpdateNote && noteTitle.isEmpty()) {
            viewModal.deleteNote(note.taskId)

        } else if (isUpdateNote && noteDescription.isEmpty()) {
            viewModal.deleteNote(note.taskId)

        } else if (isUpdateNote && noteTitle.isEmpty() && noteDescription.isEmpty()) {
            viewModal.deleteNote(note.taskId)

        }
    }

    private fun showKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

}