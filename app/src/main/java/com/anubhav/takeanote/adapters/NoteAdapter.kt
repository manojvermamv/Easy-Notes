package com.anubhav.takeanote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.takeanote.BR
import com.anubhav.takeanote.database.model.Note

class NoteAdapter(
    private val context: Context,
    private val noteItemClickInterface: NoteItemClickInterface
) :
    ListAdapter<Note, BindViewHolder>(NoteDiffUtil()) {

    private val viewTypeToLayoutId: MutableMap<Int, Int> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            viewTypeToLayoutId[viewType] ?: 0,
            parent,
            false
        )
        return BindViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindViewHolder, position: Int) {
        holder.bind(getItem(position), noteItemClickInterface)
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (!viewTypeToLayoutId.containsKey(item.viewType)) {
            viewTypeToLayoutId[item.viewType] = item.layoutId
        }
        return item.viewType
    }

}

class BindViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    // on below line we are creating an initializing all our
    // variables which we have added in layout file.
    fun bind(note: Note, noteItemClickInterface: NoteItemClickInterface) {
        binding.setVariable(BR.note, note)
        binding.setVariable(BR.onNoteItemClick, noteItemClickInterface)
    }

}

class NoteDiffUtil : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.taskId == newItem.taskId
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }

}

interface NoteItemClickInterface {
    fun onNoteClick(note: Note);
    fun onNoteDeleteClick(note: Note)
}