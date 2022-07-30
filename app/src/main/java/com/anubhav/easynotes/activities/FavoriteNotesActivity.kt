package com.anubhav.easynotes.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.commonutility.ItemSwipeHelper
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.easynotes.R
import com.anubhav.easynotes.adapters.NoteAdapter
import com.anubhav.easynotes.adapters.NoteItemClickInterface
import com.anubhav.easynotes.database.model.Note
import com.anubhav.easynotes.database.model.getEmptyItem
import com.anubhav.easynotes.databinding.ActivityFavoriteNotesBinding
import com.anubhav.easynotes.utils.GlobalData
import com.anubhav.easynotes.utils.HelperMethod
import com.anubhav.easynotes.viewmodel.NoteViewModal
import com.google.android.material.snackbar.Snackbar
import java.util.*

class FavoriteNotesActivity : AppCompatActivity(), NoteItemClickInterface {

    lateinit var binding: ActivityFavoriteNotesBinding

    // on below line we are creating a variable
    // for our recycler view, exit text, button and viewModel.
    private lateinit var viewModal: NoteViewModal
    private lateinit var noteRVAdapter: NoteAdapter

    companion object {
        val TAG: String = FavoriteNotesActivity::class.simpleName as String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_favorite_notes)
        //excludeStatusAndNavBarFromTransition(window)
        GlobalData.setStatusBarFullScreen(this)
        setViewHeight(binding.topView)

        FontUtils.setFont(this, binding.root as ViewGroup)

        // on below line we are initializing our view modal.
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModal::class.java)

        onCreateActionBar()
        initRecyclerView()
    }

    private fun onCreateActionBar() {
        binding.layActionbar.actionBarTitle = getString(R.string.label_favorites)
        binding.layActionbar.menuVisible = false

        binding.layActionbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun initRecyclerView() {
        // on below line we are setting layout
        // manager to our recycler view.
        binding.notesRV.layoutManager = LinearLayoutManager(this)

        // on below line we are initializing our adapter class.
        noteRVAdapter = NoteAdapter(this, this)
        noteRVAdapter.longClickEnabled = false

        // on below line we are setting
        // adapter to our recycler view.
        binding.notesRV.adapter = noteRVAdapter

        // on below line we are calling all notes method
        // from our view modal class to observer the changes on list.
        viewModal.allFavoriteNotes.observe(this) { list ->
            list?.let {
                val itemList: MutableList<Note> = list.toMutableList()
                if (itemList.isEmpty()) {
                    itemList.add(getEmptyItem())
                }
                // on below line we are updating our list.
                noteRVAdapter.submitList(itemList)
            }
        }

        val itemTouchHelper = ItemTouchHelper(
            ItemSwipeHelper(
                this, onItemSwipeListener, HelperMethod.dpToPx(this, 18).toFloat()
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.notesRV)
    }


    /**
     * SharedPreferences from settings preferences
     * */

    private var isAnimationEnabled = true

    private fun initSharedPreferences() {
        val prefsValues = resources.getStringArray(R.array.pref_values)
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        isAnimationEnabled = sharedPrefs.getBoolean(getString(R.string.pref_edit_note_anim), true)

        val prefSortBy =
            sharedPrefs.getString(getString(R.string.pref_sortby), prefsValues[0]) as String
        val prefSortByTitle = getListPreferencesTitle(prefSortBy, R.array.pref_sortby_entries)

    }

    private fun getListPreferencesTitle(value: Any, @ArrayRes entries: Int): String {
        val entryValues = resources.getStringArray(R.array.pref_values);
        val prefIndex: Int = Arrays.binarySearch(entryValues, value)
        return resources.getStringArray(entries)[prefIndex]
    }

    override fun onResume() {
        super.onResume()
        initSharedPreferences()
        viewModal.updateNotes()
    }

    override fun onItemClick(view: View, position: Int, note: Note) {
        AddEditNoteActivity.start(this, view, note, isAnimationEnabled)
    }

    override fun onItemDeleteClick(position: Int, note: Note) {
    }

    override fun onItemSelectionEnabled(enabled: Boolean) {
    }

    override fun onItemSelectionChanged(selectionListCounts: Int) {
    }

    private var onItemSwipeListener: ItemSwipeHelper.OnSwipeListener =
        object : ItemSwipeHelper.OnSwipeListener {
            override fun onItemLeftSwipe(viewHolder: RecyclerView.ViewHolder?, position: Int) {
                val note: Note = noteRVAdapter.currentList[position]
                showNoteDeleteDialog(note)
            }

            override fun onItemRightSwipe(viewHolder: RecyclerView.ViewHolder?, position: Int) {
                val note: Note = noteRVAdapter.currentList[position]
                showSavedSnackbar(!note.isFavorite)
                viewModal.updateFavoriteNote(note)
            }
        }

    private fun showNoteDeleteDialog(note: Note) {
        val dialog = Dialog(this, R.style.CustomDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_delete_note)

        FontUtils.setFont(this, dialog.findViewById(R.id.root_view))

        val textTitle = dialog.findViewById<View>(R.id.dialog_title) as TextView
        textTitle.setText(R.string.confirm_delete_title)
        val textDesc = dialog.findViewById<View>(R.id.dialog_desc) as TextView
        textDesc.setText(R.string.confirm_delete_desc)

        val declineDialogButton = dialog.findViewById<Button>(R.id.bt_decline)
        declineDialogButton.setOnClickListener { dialog.dismiss() }

        val confirmDialogButton = dialog.findViewById<Button>(R.id.bt_confirm)
        confirmDialogButton.setOnClickListener {
            viewModal.deleteNote(note)
            dialog.dismiss()
            showDeleteSnackbar(note)
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    fun showDeleteSnackbar(note: Note) {
        val snackbar = Snackbar.make(binding.rootView, "", Snackbar.LENGTH_LONG)

        val customSnackView: View = layoutInflater.inflate(R.layout.custom_snackbar_view, null)

        snackbar.view.setBackgroundColor(Color.TRANSPARENT)

        // now change the layout of the snackbar
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout

        // set padding of the all corners as 0
        snackbarLayout.setPadding(0, 0, 0, 0)

        FontUtils.setFont(this, customSnackView.findViewById(R.id.root_view))

        val tvTitle = customSnackView.findViewById<TextView>(R.id.tv_title)
        val tvDesc = customSnackView.findViewById<TextView>(R.id.tv_desc)
        tvTitle.text = "Deleted"
        tvDesc.text = "This note going to delete!"

        val imgView = customSnackView.findViewById<ImageView>(R.id.imageView)
        imgView.setImageResource(R.drawable.ic_round_delete_outline_24)
        imgView.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_500))

        val btnAction = customSnackView.findViewById<Button>(R.id.action_button)
        btnAction.text = "Undo"
        btnAction.setOnClickListener {
            viewModal.addNote(note)
            snackbar.dismiss()
        }

        snackbarLayout.addView(customSnackView, 0)
        snackbar.show()
    }

    @SuppressLint("SetTextI18n")
    fun showSavedSnackbar(isAddedToFavorite: Boolean) {
        val snackbar = Snackbar.make(binding.rootView, "", Snackbar.LENGTH_LONG)

        val customSnackView: View = layoutInflater.inflate(R.layout.custom_snackbar_view, null)

        snackbar.view.setBackgroundColor(Color.TRANSPARENT)

        // now change the layout of the snackbar
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout

        // set padding of the all corners as 0
        snackbarLayout.setPadding(0, 0, 0, 0)

        FontUtils.setFont(this, customSnackView.findViewById(R.id.root_view))

        val tvTitle = customSnackView.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = if (isAddedToFavorite) "Added to favorites." else "Removed from favorites."

        val tvDesc = customSnackView.findViewById<TextView>(R.id.tv_desc)
        tvDesc.visibility = View.GONE

        val imgView = customSnackView.findViewById<ImageView>(R.id.imageView)
        imgView.setImageResource(R.drawable.ic_round_favorite_border_24)
        imgView.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary))

        val btnAction = customSnackView.findViewById<Button>(R.id.action_button)
        btnAction.text = if (isAddedToFavorite) "Favorites" else "OK"
        btnAction.setOnClickListener {
            snackbar.dismiss()
            if (isAddedToFavorite) {
                startActivity(Intent(this, FavoriteNotesActivity::class.java))
            }
        }

        snackbarLayout.addView(customSnackView, 0)
        snackbar.show()
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