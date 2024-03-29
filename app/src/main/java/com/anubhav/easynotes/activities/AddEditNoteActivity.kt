package com.anubhav.easynotes.activities

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.easynotes.R
import com.anubhav.easynotes.database.model.Note
import com.anubhav.easynotes.databinding.ActivityAddEditNoteBinding
import com.anubhav.easynotes.utils.DateTimeUtils
import com.anubhav.easynotes.utils.GlobalData
import com.anubhav.easynotes.utils.HelperMethod
import com.anubhav.easynotes.viewmodel.NoteViewModal
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import org.threeten.bp.OffsetDateTime


class AddEditNoteActivity : AppCompatActivity() {

    companion object {
        val TAG = AddEditNoteActivity::class.simpleName as String

        fun start(activity: Activity, sharedView: View, note: Note?, hasAnimation: Boolean = true) {
            val intent = Intent(activity, AddEditNoteActivity::class.java)
            // opening a new intent and passing a data to it
            intent.putExtra("hasAnimation", hasAnimation)
            intent.putExtra("updateNote", note != null)
            if (note != null) intent.putExtra("noteData", note)

            if (!hasAnimation) {
                activity.startActivity(intent)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val options: ActivityOptions =
                    ActivityOptions.makeSceneTransitionAnimation(
                        activity, sharedView, TAG
                    )
                activity.startActivity(intent, options.toBundle())
            } else {
                val activityOptions: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, sharedView, TAG
                    )
                ActivityCompat.startActivity(activity, intent, activityOptions.toBundle())
            }
        }

    }

    lateinit var binding: ActivityAddEditNoteBinding
    private var isUpdateNote: Boolean = false
    private var isDeleteBtnClicked: Boolean = false

    // on below line we are creating variable for
    // viewModal and and integer for our note id.
    lateinit var viewModal: NoteViewModal
    lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        if (intent.getBooleanExtra("hasAnimation", true)) {
            // Set up shared element transition
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                findViewById<View>(android.R.id.content).transitionName = TAG
                setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
                window.sharedElementEnterTransition = buildContainerTransform(true)
                window.sharedElementReturnTransition = buildContainerTransform(false)
                window.sharedElementsUseOverlay = true;
            } else {
                ViewCompat.setTransitionName(findViewById<View>(android.R.id.content), TAG)
            }
        }

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_edit_note)
        //excludeStatusAndNavBarFromTransition(window)
        GlobalData.setStatusBarFullScreen(this)
        setViewHeight(binding.topView)

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

        binding.layActionbar.imgBack.setOnClickListener { onBackPressed() }

        binding.layActionbar.btnShare.setOnClickListener { shareNote() }

        binding.layActionbar.btnDelete.setOnClickListener {
            isDeleteBtnClicked = true
            if (isUpdateNote) viewModal.deleteNote(note.taskId)
            onBackPressed()
        }
    }

    private fun onCreateApp() {
        if (isUpdateNote) {
            binding.saveBtn.setText(R.string.update)
            binding.edtNoteName.setText(note.noteTitle)
            binding.edNoteDesc.setText(note.noteDescription)
        } else {
            binding.saveBtn.setText(R.string.save)
        }
        binding.saveBtn.setOnClickListener { onBackPressed() }

        // on below line we are initialing our view modal.
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModal::class.java)
    }

    override fun onBackPressed() {
        if (isDeleteBtnClicked) {
            isDeleteBtnClicked = false
        } else {
            saveNote()
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        binding.edNoteDesc.isFocusable = true
        showKeyboard()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun buildContainerTransform(entering: Boolean): MaterialContainerTransform {
        val transform = MaterialContainerTransform()
        transform.transitionDirection =
            if (entering) MaterialContainerTransform.TRANSITION_DIRECTION_ENTER else MaterialContainerTransform.TRANSITION_DIRECTION_RETURN
        transform.setAllContainerColors(
            MaterialColors.getColor(findViewById(android.R.id.content), R.attr.colorSurface)
        )
        transform.addTarget(android.R.id.content)
        transform.fitMode = MaterialContainerTransform.FIT_MODE_AUTO
        transform.pathMotion = MaterialArcMotion()
        transform.interpolator = FastOutSlowInInterpolator()
        transform.duration = 540L
        return transform
    }

    private fun saveNote() {
        val noteTitle = binding.edtNoteName.text.toString().trim()
        val noteDescription = binding.edNoteDesc.text.toString().trim()

        if (noteTitle.isNotEmpty() || noteDescription.isNotEmpty()) {
            val currentTime: String = DateTimeUtils.getCurrentTime()
            val currentTimeDate: OffsetDateTime = OffsetDateTime.now()
            if (isUpdateNote) {
                if (!(noteTitle == note.noteTitle && noteDescription == note.noteDescription)) {
                    val updatedNote = Note(noteTitle, noteDescription, currentTime, currentTimeDate)
                    updatedNote.taskId = note.taskId
                    updatedNote.isFavorite = note.isFavorite
                    viewModal.updateNote(updatedNote)
                }
            } else {
                viewModal.addNote(Note(noteTitle, noteDescription, currentTime, currentTimeDate))
            }
        }

        if (isUpdateNote && (noteTitle.isNotEmpty() || noteDescription.isNotEmpty())) {
            if (!(noteTitle == note.noteTitle && noteDescription == note.noteDescription)) {
                val currentTime: String = DateTimeUtils.getCurrentTime()
                val currentTimeDate: OffsetDateTime = OffsetDateTime.now()
                val updatedNote = Note(noteTitle, noteDescription, currentTime, currentTimeDate)
                updatedNote.taskId = note.taskId
                updatedNote.isFavorite = note.isFavorite
                viewModal.updateNote(updatedNote)
            }
        } else if (isUpdateNote && noteTitle.isEmpty() && noteDescription.isEmpty()) {
            viewModal.deleteNote(note.taskId)
        }
    }

    private fun shareNote() {
        val noteTitle = binding.edtNoteName.text.toString().trim()
        val noteDescription = binding.edNoteDesc.text.toString().trim()

        if (noteTitle.isNotEmpty() || noteDescription.isNotEmpty()) {
            val sb = StringBuilder()
            if (noteTitle.isNotEmpty()) {
                sb.append(noteTitle).append("\n")
            }
            if (noteDescription.isNotEmpty()) {
                sb.append(noteDescription)
            }
            GlobalData.shareText(this, sb.toString())
        } else {
            Toast.makeText(this, "Note is empty", Toast.LENGTH_SHORT).show()
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

    private fun setViewHeight(view: View) {
        val params: ViewGroup.LayoutParams = view.layoutParams
        params.height = HelperMethod.getStatusBarHeight(this)
        view.layoutParams = params
    }

}