package com.anubhav.takeanote.rvadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.takeanote.R
import com.anubhav.takeanote.database.model.Note

class NoteAdapter(private val context: Context, private val noteItemClickInterface: NoteItemClickInterface) :
    ListAdapter<Note, NoteAdapter.ViewHolder>(NoteDiffUtil()) {

    // on below line we are creating a view holder class.
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // on below line we are creating an initializing all our
        // variables which we have added in layout file.
        val noteTV = itemView.findViewById<TextView>(R.id.idTVNote)
        val dateTV = itemView.findViewById<TextView>(R.id.idTVDate)
        val deleteIV = itemView.findViewById<ImageView>(R.id.idIVDelete)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflating our layout file for each item of recycler view.
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.note_rv_item,
            parent, false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.noteTV.text = getItem(position).noteTitle
        holder.dateTV.text = "Last Updated : " + getItem(position).timeStamp

        holder.deleteIV.setOnClickListener {
            noteItemClickInterface.onNoteDeleteClick(getItem(position))
        }

        holder.itemView.setOnClickListener {
            noteItemClickInterface.onNoteClick(getItem(position))
        }
    }

}

interface NoteItemClickInterface {
    fun onNoteClick(note: Note);
    fun onNoteDeleteClick(note: Note)
}

class NoteDiffUtil : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.taskId == newItem.taskId
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }

}