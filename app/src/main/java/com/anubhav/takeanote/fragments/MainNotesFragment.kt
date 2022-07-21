package com.anubhav.takeanote.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.commonutility.ItemSwipeHelper
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.takeanote.R
import com.anubhav.takeanote.activities.AddEditNoteActivity
import com.anubhav.takeanote.activities.FavoriteNotesActivity
import com.anubhav.takeanote.activities.MainActivity
import com.anubhav.takeanote.adapters.NoteAdapter
import com.anubhav.takeanote.adapters.NoteItemClickInterface
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.database.model.getEmptyItem
import com.anubhav.takeanote.databinding.FragMainNotesBinding
import com.anubhav.takeanote.utils.GlobalData
import com.anubhav.takeanote.utils.HelperMethod
import com.anubhav.takeanote.viewmodel.NoteViewModal
import com.google.android.material.snackbar.Snackbar


private const val ARG_PARAM1 = "param1"

class MainNotesFragment() : Fragment(), NoteItemClickInterface {

    var param1: String? = null
    lateinit var binding: FragMainNotesBinding

    // on below line we are creating a variable
    // for our recycler view, exit text, button and viewModel.
    private lateinit var viewModal: NoteViewModal
    private lateinit var noteRVAdapter: NoteAdapter

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
            AddEditNoteActivity.start(requireActivity(), binding.addFab, null, true)
        }

        initSearchView()
        initRecyclerView()
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

        // on below line we are calling all notes method
        // from our view modal class to observer the changes on list.
        viewModal.allNotes.observe(viewLifecycleOwner) { list ->
            list?.let { updateRecyclerAdapter(it.toMutableList()) }
        }
    }

    private fun initSearchView() {
        val imageTintColor: ColorStateList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.imagetintcolor_grey
                )
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
        val searchAutoComplete =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
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
                updateRecyclerAdapter(viewModal.searchNotes(query))
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                updateRecyclerAdapter(viewModal.searchNotes(query))
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModal.updateNotes()
    }

    private fun updateRecyclerAdapter(itemList: MutableList<Note>) {
        if (itemList.isEmpty()) {
            itemList.add(getEmptyItem())
        }

        // on below line we are updating our list.
        noteRVAdapter.submitList(itemList)

        val itemTouchHelper = ItemTouchHelper(
            ItemSwipeHelper(
                context, itemSwipeListener, HelperMethod.dpToPx(context, 18).toFloat()
            )
        )
        itemTouchHelper.attachToRecyclerView(null)
        itemTouchHelper.attachToRecyclerView(binding.notesRV)
    }

    override fun onItemClick(view: View, position: Int, note: Note) {
        if (selectModeEnabled) {
            setSelection(position, note)
        } else {
            AddEditNoteActivity.start(requireActivity(), view, note, true)
        }
    }

    override fun onItemDeleteClick(position: Int, note: Note) {
    }

    override fun onItemLongClick(view: View, position: Int, note: Note) {
        if (!selectModeEnabled) {
            val activity: Activity = requireActivity()
            if (activity is MainActivity) {
                activity.setSelectionActionBar(true)
            }
        }
        setSelection(position, note)
    }

    override fun onItemSelectionClick(view: View, position: Int, note: Note) {
        setSelection(position, note)
    }

    /**
     * custom method for multi items selection
     * */

    var selectModeEnabled: Boolean = false
    var selectionList: MutableList<Note> = mutableListOf()

    private fun setSelection(position: Int, note: Note) {
        selectModeEnabled = true
        noteRVAdapter.selectionMode = true
        note.isSelected = !note.isSelected
        if (selectionList.contains(note)) {
            selectionList.remove(note)
        } else {
            selectionList.add(note)
        }
        noteRVAdapter.notifyItemChanged(position)
        onItemSelectionChanged()
    }

    private fun clearSelection() {
        selectModeEnabled = false
        noteRVAdapter.selectionMode = false
        selectionList.clear()
        val list = noteRVAdapter.currentList
        list.forEach {
            it.isSelected = false
        }
        noteRVAdapter.submitList(list)
        onItemSelectionChanged()
    }

    // https://bignerdranch.github.io/recyclerview-multiselect/
    private fun selectAll(selectedAll: Boolean) {
        selectModeEnabled = true
        noteRVAdapter.selectionMode = true
        selectionList.clear()
        val list = noteRVAdapter.currentList
        list.forEach {
            it.isSelected = selectedAll
            if (selectedAll) selectionList.add(it)
        }
        noteRVAdapter.submitList(list)
        onItemSelectionChanged()
    }

    private fun onItemSelectionChanged() {
        // here get selection list size
        val activity: Activity = requireActivity()
        if (activity is MainActivity) {
            activity.binding.txtSelectionCount.text =
                String.format(getString(R.string.selected_item), selectionList.size)

            activity.binding.imgSelectionClose.setOnClickListener {
                activity.setSelectionActionBar(false)
                clearSelection()
            }
            activity.binding.imgSelectionAll.setOnClickListener {
                if (noteRVAdapter.currentList.size == selectionList.size) {
                    activity.binding.imgSelectionAll.isSelected = false
                    selectAll(false)
                } else {
                    activity.binding.imgSelectionAll.isSelected = true
                    selectAll(true)
                }
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


    /**
     * methods for dialogs and ui operations
     *
     * */

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