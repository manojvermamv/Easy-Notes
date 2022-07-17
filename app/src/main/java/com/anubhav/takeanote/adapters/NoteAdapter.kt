package com.anubhav.takeanote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.takeanote.BR
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.utils.DateTimeUtils

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

        //if (viewType == R.layout.item_not_found) {
        //    return EmptyViewHolder(binding)
        //}
        FontUtils.setFont(context, binding.root as ViewGroup)
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
        binding.setVariable(BR.displayTime, DateTimeUtils.getDisplayTime(note.timeStamp))
        binding.setVariable(BR.note, note)
        binding.setVariable(BR.onNoteItemClick, noteItemClickInterface)
    }

}

class NoteDiffUtil : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.taskId == newItem.taskId
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return (oldItem.noteTitle == newItem.noteTitle)
                && (oldItem.noteDescription == newItem.noteDescription)
                && (oldItem.timeStamp == newItem.timeStamp)
    }

}

interface NoteItemClickInterface {
    fun onNoteClick(view: View, note: Note);
    fun onNoteDeleteClick(note: Note)
}