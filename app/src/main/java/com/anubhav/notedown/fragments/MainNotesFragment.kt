package com.anubhav.notedown.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.commonutility.ItemSwipeHelper
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.notedown.R
import com.anubhav.notedown.activities.AddEditNoteActivity
import com.anubhav.notedown.activities.FavoriteNotesActivity
import com.anubhav.notedown.activities.MainActivity
import com.anubhav.notedown.adapters.NoteAdapter
import com.anubhav.notedown.adapters.NoteItemClickInterface
import com.anubhav.notedown.database.model.Note
import com.anubhav.notedown.database.model.getEmptyItem
import com.anubhav.notedown.databinding.FragMainNotesBinding
import com.anubhav.notedown.utils.GlobalData
import com.anubhav.notedown.utils.HelperMethod
import com.anubhav.notedown.viewmodel.NoteViewModal
import com.google.android.material.snackbar.Snackbar
import java.util.*


private const val ARG_PARAM1 = "param1"

class MainNotesFragment() : Fragment(), NoteItemClickInterface {

    var param1: String? = null
    lateinit var binding: FragMainNotesBinding

    // on below line we are creating a variable
    // for our recycler view, exit text, button and viewModel.
    lateinit var viewModal: NoteViewModal
    lateinit var noteRVAdapter: NoteAdapter

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            MainNotesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_main_notes, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FontUtils.setFont(context, binding.root as ViewGroup)

        // on below line we are initializing our view modal.
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(NoteViewModal::class.java)

        binding.addFab.setOnClickListener {
            AddEditNoteActivity.start(requireActivity(), binding.addFab, null, isAnimationEnabled)
        }

        initSearchView()
        initRecyclerView()

        /// init bottom view here
        binding.layBottomview.visibility = View.GONE
        binding.imgDeleteall.setOnClickListener { onDeleteAllClick() }
        binding.imgShareall.setOnClickListener { onShareAllClick() }
    }

    private fun initSearchView() {
        val imageTintColor: ColorStateList =
            ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.imagetintcolor_grey)
            )
        val searchManager: SearchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val searchView = binding.searchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView.maxWidth = Int.MAX_VALUE

        /*Expanding the search view */
        //searchView.isIconified = false
        searchView.setIconifiedByDefault(false)

        /* Code for changing the text color and hint color for the search view */
        val searchAutoComplete: SearchView.SearchAutoComplete =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        searchAutoComplete.setTextColor(resources.getColor(R.color.fontcoloreditext))
        searchAutoComplete.setHintTextColor(resources.getColor(R.color.fontcoloreditexthint))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            searchAutoComplete.setTextCursorDrawable(R.drawable.cursor_search)
        }

        /*Code for changing the search icon */
        val searchIcon =
            searchView.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        searchIcon.setImageResource(R.drawable.ic_round_search_24)
        searchIcon.imageTintList = imageTintColor

        /*Code for changing the voice search icon
        val voiceIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn) as ImageView
        voiceIcon.setImageResource(R.drawable.ic_round_mic_none_24);
        voiceIcon.imageTintList = imageTintColor;*/

        GlobalData.setSearchViewCursor(searchView, R.drawable.cursor_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                noteRVAdapter.submitSearchQuery(query)
                updateRecyclerAdapter(viewModal.searchNotes(query))
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                noteRVAdapter.submitSearchQuery(query)
                updateRecyclerAdapter(viewModal.searchNotes(query))
                return true
            }
        })
    }

    private fun initRecyclerView() {
        // on below line we are setting layout
        // manager to our recycler view.
        binding.notesRV.layoutManager = LinearLayoutManager(context)

        // on below line we are initializing our adapter class.
        noteRVAdapter = NoteAdapter(requireContext(), this)

        // on below line we are setting
        // adapter to our recycler view.
        binding.notesRV.adapter = noteRVAdapter

        // attach swipe helper to recycler view
        val itemTouchHelper = ItemTouchHelper(
            ItemSwipeHelper(
                context, itemSwipeListener, HelperMethod.dpToPx(context, 18).toFloat()
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.notesRV)

        // on below line we are calling all notes method
        // from our view modal class to observer the changes on list.
        viewModal.allNotes.observe(viewLifecycleOwner) { list ->
            list?.let { updateRecyclerAdapter(it.toMutableList()) }
        }
    }

    private fun onDeleteAllClick() {
        if (!noteRVAdapter.selectionMode) return
        if (noteRVAdapter.selectionList.isEmpty()) return
        showMultipleNoteDeleteDialog(noteRVAdapter.selectionList)
    }

    private fun onShareAllClick() {
        if (!noteRVAdapter.selectionMode) return
        if (noteRVAdapter.selectionList.isEmpty()) return
    }

    /**
     * SharedPreferences from settings preferences
     * */

    private var isAnimationEnabled = true

    private fun initSharedPreferences() {
        val prefsValues = resources.getStringArray(R.array.pref_values)
        val sharedPrefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())

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
        viewModal.updateNotes()
        initSharedPreferences()
    }

    private fun updateRecyclerAdapter(itemList: MutableList<Note>) {
        // on below line we are updating our list.

        if (itemList.isEmpty()) {
            itemList.add(getEmptyItem())
            noteRVAdapter.submitList(itemList) {
                noteRVAdapter.clearSelection()
            }
        } else {
            noteRVAdapter.submitList(itemList) {
                noteRVAdapter.refreshSelection()
            }
        }
    }

    private var itemSwipeListener: ItemSwipeHelper.OnSwipeListener =
        object : ItemSwipeHelper.OnSwipeListener {
            override fun onItemLeftSwipe(viewHolder: RecyclerView.ViewHolder, position: Int) {
                val note: Note = noteRVAdapter.currentList[position]
                showNoteDeleteDialog(note)
            }

            override fun onItemRightSwipe(viewHolder: RecyclerView.ViewHolder, position: Int) {
                val note: Note = noteRVAdapter.currentList[position]
                showSavedSnackbar(!note.isFavorite)
                viewModal.updateFavoriteNote(note)
            }
        }


    override fun onItemClick(view: View, position: Int, note: Note) {
        AddEditNoteActivity.start(requireActivity(), view, note, isAnimationEnabled)
    }

    override fun onItemDeleteClick(position: Int, note: Note) {
    }

    override fun onItemSelectionEnabled(enabled: Boolean) {
        //searchAutoComplete.isEnabled = !enabled
        binding.addFab.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.layBottomview.visibility = if (enabled) View.VISIBLE else View.GONE
        val activity: Activity = requireActivity()
        if (activity is MainActivity) {
            activity.setSelectionActionBar(enabled)
            activity.binding.imgSelectionClose.setOnClickListener {
                noteRVAdapter.clearSelection()
            }
            activity.binding.imgSelectionAll.setOnClickListener {
                noteRVAdapter.setAllItemSelected()
            }
        }
    }

    override fun onItemSelectionChanged(selectionListCounts: Int) {
        // here get selection list counts and update ui according it
        val activity: Activity = requireActivity()
        if (activity is MainActivity) {
            activity.binding.txtSelectionCount.text =
                String.format(getString(R.string.selected_item), selectionListCounts)

            activity.binding.imgSelectionAll.isSelected = noteRVAdapter.isAllItemSelected()
        }
    }


    private fun showNoteDeleteDialog(note: Note) {
        val dialog = Dialog(requireContext(), R.style.CustomDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_delete_note)

        FontUtils.setFont(context, dialog.findViewById(R.id.root_view))

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

    private fun showMultipleNoteDeleteDialog(list: MutableList<Int>) {
        val dialog = Dialog(requireContext(), R.style.CustomDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_delete_note)

        FontUtils.setFont(context, dialog.findViewById(R.id.root_view))

        val textTitle = dialog.findViewById<View>(R.id.dialog_title) as TextView
        textTitle.setText(R.string.confirm_delete_title)

        val textDesc = dialog.findViewById<View>(R.id.dialog_desc) as TextView
        textDesc.text = String.format(getString(R.string.confirm_deleteall_desc), list.size)

        val declineDialogButton = dialog.findViewById<Button>(R.id.bt_decline)
        declineDialogButton.setOnClickListener { dialog.dismiss() }

        val confirmDialogButton = dialog.findViewById<Button>(R.id.bt_confirm)
        confirmDialogButton.setOnClickListener {
            dialog.dismiss()
            list.forEach {
                viewModal.deleteNote(it)
            }
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

        FontUtils.setFont(requireContext(), customSnackView.findViewById(R.id.root_view))

        val tvTitle = customSnackView.findViewById<TextView>(R.id.tv_title)
        val tvDesc = customSnackView.findViewById<TextView>(R.id.tv_desc)
        tvTitle.text = "Deleted"
        tvDesc.text = "This note going to delete!"

        val imgView = customSnackView.findViewById<ImageView>(R.id.imageView)
        imgView.setImageResource(R.drawable.ic_round_delete_outline_24)
        imgView.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red_500))

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

        FontUtils.setFont(requireContext(), customSnackView.findViewById(R.id.root_view))

        val tvTitle = customSnackView.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = if (isAddedToFavorite) "Added to favorites." else "Removed from favorites."

        val tvDesc = customSnackView.findViewById<TextView>(R.id.tv_desc)
        tvDesc.visibility = View.GONE

        val imgView = customSnackView.findViewById<ImageView>(R.id.imageView)
        imgView.setImageResource(R.drawable.ic_round_favorite_border_24)
        imgView.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        val btnAction = customSnackView.findViewById<Button>(R.id.action_button)
        btnAction.text = if (isAddedToFavorite) "Favorites" else "OK"
        btnAction.setOnClickListener {
            snackbar.dismiss()
            if (isAddedToFavorite) {
                startActivity(Intent(requireContext(), FavoriteNotesActivity::class.java))
            }
        }

        snackbarLayout.addView(customSnackView, 0)
        snackbar.show()
    }

}