package com.anubhav.takeanote.fragments

import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.anubhav.commonutility.ItemSwipeHelper
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.takeanote.R
import com.anubhav.takeanote.activities.AddEditNoteActivity
import com.anubhav.takeanote.adapters.NoteAdapter
import com.anubhav.takeanote.adapters.NoteItemClickInterface
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.databinding.FragMainNotesBinding
import com.anubhav.takeanote.interfaces.RvItemSwipeListener
import com.anubhav.takeanote.utils.GlobalData
import com.anubhav.takeanote.viewmodel.NoteViewModal


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
            AddEditNoteActivity.start(requireActivity(), binding.addFab, null, false)
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
            list?.let {
                // on below line we are updating our list.
                binding.layNotFound.rootView.visibility =
                    if (it.isEmpty()) View.VISIBLE else View.GONE
                noteRVAdapter.submitList(it)
            }
        }

        val itemTouchHelper = ItemTouchHelper(ItemSwipeHelper(itemSwipeListener, context))
        itemTouchHelper.attachToRecyclerView(binding.notesRV)
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
        searchView.isIconified = false
        searchView.setIconifiedByDefault(false)

        /* Code for changing the text color and hint color for the search view */
        val searchAutoComplete =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        searchAutoComplete.setTextColor(resources.getColor(R.color.fontcoloreditext))
        searchAutoComplete.setHintTextColor(resources.getColor(R.color.fontcoloreditexthint))

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
                val list = viewModal.searchNotes(query)
                binding.layNotFound.rootView.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
                noteRVAdapter.submitList(list)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                val list = viewModal.searchNotes(query)
                binding.layNotFound.rootView.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
                noteRVAdapter.submitList(list)
                return false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModal.updateNotes()
    }

    override fun onNoteClick(view: View, note: Note) {
        AddEditNoteActivity.start(requireActivity(), view, note, false)
    }

    override fun onNoteDeleteClick(note: Note) {
        showNoteDeleteDialog(note)
    }

    private var itemSwipeListener: RvItemSwipeListener = object : RvItemSwipeListener {
        override fun onItemDelete(position: Int) {
            val note: Note = noteRVAdapter.currentList[position]
            showNoteDeleteDialog(note)
        }

        override fun onItemFavorite(position: Int) {
        }
    }

    private fun showNoteDeleteDialog(note: Note) {
        val dialog = Dialog(requireContext(), R.style.CustomDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_delete_note)

        FontUtils.setFont(context, dialog.findViewById(R.id.root_view))

        val textTitle = dialog.findViewById<View>(R.id.dialog_title) as TextView
        textTitle.setText(R.string.confirm_delete_title)
        val textDesc = dialog.findViewById<View>(R.id.dialog_desc) as TextView
        textDesc.setText(R.string.confirm_delete_desc)

        val declineDialogButton = dialog.findViewById<Button>(R.id.bt_decline)
        declineDialogButton.setOnClickListener {
            noteRVAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        val confirmDialogButton = dialog.findViewById<Button>(R.id.bt_confirm)
        confirmDialogButton.setOnClickListener {
            viewModal.deleteNote(note)
            Toast.makeText(context, "${note.noteTitle} Deleted", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

}