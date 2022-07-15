package com.anubhav.takeanote.activities

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.takeanote.R
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.databinding.ActivityAddEditNoteBinding
import com.anubhav.takeanote.utils.DateTimeUtils
import com.anubhav.takeanote.viewmodel.NoteViewModal
import java.text.SimpleDateFormat
import java.util.*

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

        // on below line we are adding
        // click listener to our save button.
        binding.saveBtn.setOnClickListener {
            saveNote()
            finish()
        }

        binding.edNoteDesc.isFocusable = true
        showKeyboard(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveNote()
    }

    private fun saveNote() {
        val noteTitle = binding.edtNoteName.text.toString().trim()
        val noteDescription = binding.edNoteDesc.text.toString().trim()

        if (noteDescription.isNotEmpty()) {
            if (isUpdateNote) {
                val updatedNote = Note(noteTitle, noteDescription, DateTimeUtils.getCurrentTime())
                updatedNote.taskId = note.taskId
                viewModal.updateNote(updatedNote)
            } else {
                viewModal.addNote(Note(noteTitle, noteDescription, DateTimeUtils.getCurrentTime()))
            }
        }
    }


    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.hideSoftInputFromWindow(view!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun showKeyboard(activity: Activity) {
        val view = activity.currentFocus
        val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

}