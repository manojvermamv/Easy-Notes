package com.anubhav.takeanote.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.commonutility.SwipeDeleteHelper
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.takeanote.R
import com.anubhav.takeanote.activities.AddEditNoteActivity
import com.anubhav.takeanote.adapters.NoteAdapter
import com.anubhav.takeanote.adapters.NoteItemClickInterface
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.databinding.FragMainNotesBinding
import com.anubhav.takeanote.interfaces.RvItemDeleteListener
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
        FontUtils.setFont(requireContext(), binding.root as ViewGroup)

        // on below line we are initializing our view modal.
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(NoteViewModal::class.java)

        binding.addFab.setOnClickListener {
            val intent = Intent(requireContext(), AddEditNoteActivity::class.java)
            startActivity(intent)
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        // on below line we are setting layout
        // manager to our recycler view.
        binding.notesRV.layoutManager = LinearLayoutManager(requireContext())

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
                noteRVAdapter.submitList(it)
                binding.layNotFound.rootView.visibility =
                    if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        val itemTouchHelper =
            ItemTouchHelper(SwipeDeleteHelper(itemDeleteListener, requireContext()))
        itemTouchHelper.attachToRecyclerView(binding.notesRV)
    }

    override fun onResume() {
        super.onResume()
        viewModal.getAllNotes()
    }

    override fun onNoteClick(note: Note) {
        // opening a new intent and passing a data to it.
        val intent = Intent(requireContext(), AddEditNoteActivity::class.java)
        intent.putExtra("updateNote", true)
        intent.putExtra("noteData", note)
        startActivity(intent)
    }

    override fun onNoteDeleteClick(note: Note) {
        viewModal.deleteNote(note)
        Toast.makeText(requireContext(), "${note.noteTitle} Deleted", Toast.LENGTH_SHORT).show()
    }

    var itemDeleteListener: RvItemDeleteListener = object : RvItemDeleteListener {
        override fun onItemDelete(position: Int) {
            val note: Note = noteRVAdapter.currentList[position]
            viewModal.deleteNote(note)
            Toast.makeText(requireContext(), "${note.noteTitle} Deleted", Toast.LENGTH_SHORT).show()
        }
    }

}