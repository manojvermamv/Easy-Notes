package com.anubhav.notedown.adapters

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.notedown.R
import com.anubhav.notedown.base.EmptyViewHolder
import com.anubhav.notedown.database.model.Note
import com.anubhav.notedown.databinding.ItemNotFoundBinding
import com.anubhav.notedown.databinding.NoteRvItemBinding
import com.anubhav.notedown.utils.DateTimeUtils


class NoteAdapter(
    private val context: Context, private val noteItemClickInterface: NoteItemClickInterface
) : ListAdapter<Note, RecyclerView.ViewHolder>(NoteDiffUtil()) {

    private val viewTypeToLayoutId: MutableMap<Int, Int> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: Any = DataBindingUtil.inflate(
            LayoutInflater.from(context), viewTypeToLayoutId[viewType] ?: 0, parent, false
        )

        // set view holder according viewType
        if (viewType == R.layout.item_not_found) {
            return EmptyViewHolder(context, binding as ItemNotFoundBinding)
        }
        return BindViewHolder(binding as NoteRvItemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BindViewHolder) {
            holder.bind(getItem(position), searchQuery)

            holder.binding.rootView.setOnClickListener {
                if (selectionMode) {
                    setSelection(position, getItem(position))
                } else {
                    noteItemClickInterface.onItemClick(it, position, getItem(position))
                }
            }

            holder.binding.rootView.setOnLongClickListener {
                if (longClickEnabled) {
                    if (!selectionMode) {
                        noteItemClickInterface.onItemSelectionEnabled(true)
                    }
                    setSelection(position, getItem(position))
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }

            if (selectionMode) {
                holder.binding.ivSelection.visibility = View.VISIBLE
                holder.binding.ivSelection.isSelected =
                    selectionList.contains(getItem(position).taskId)
                holder.binding.ivSelection.setOnClickListener {
                    setSelection(position, getItem(position))
                }
            } else {
                holder.binding.ivSelection.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (!viewTypeToLayoutId.containsKey(item.viewType)) {
            viewTypeToLayoutId[item.viewType] = item.layoutId
        }
        return item.viewType
    }

    /**
     * custom method for multi items selection
     * */

    var selectionMode: Boolean = false
    var longClickEnabled = true
    var selectionList: MutableList<Int> = mutableListOf()

    private fun setSelection(position: Int, note: Note) {
        selectionMode = true
        if (selectionList.contains(note.taskId)) {
            selectionList.remove(note.taskId)
        } else {
            selectionList.add(note.taskId)
        }

        notifyDataSetChanged()
        noteItemClickInterface.onItemSelectionChanged(selectionList.size)
    }

    fun refreshSelection() {
        val newSelectionList: MutableList<Int> = mutableListOf()
        currentList.forEachIndexed { _, item ->
            if (selectionList.contains(item.taskId)) {
                newSelectionList.add(item.taskId)
            }
        }

        selectionList.clear()
        selectionList.addAll(newSelectionList)
        noteItemClickInterface.onItemSelectionChanged(selectionList.size)
    }

    fun getSelectionSize(): Int {
        return selectionList.size
    }

    fun isAllItemSelected(): Boolean {
        return currentList.size == selectionList.size
    }

    fun setAllItemSelected() {
        val selectedAll = !isAllItemSelected()

        selectionMode = true
        selectionList.clear()
        if (selectedAll) {
            currentList.forEachIndexed { _, item ->
                selectionList.add(item.taskId)
            }
        }

        notifyDataSetChanged()
        noteItemClickInterface.onItemSelectionChanged(selectionList.size)
    }

    fun clearSelection() {
        if (selectionMode) {
            selectionMode = false
            selectionList.clear()
            notifyDataSetChanged()
        }
        noteItemClickInterface.onItemSelectionEnabled(false)
    }


    /**
     * custom method for search query color change
     * */

    private var searchQuery: String = ""

    fun submitSearchQuery(query: String?) {
        searchQuery = ""
        if (query != null) {
            searchQuery = query
        }
    }

}

class BindViewHolder(val binding: NoteRvItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        // set font style
        FontUtils.setFont(binding.root.context, binding.root as ViewGroup)
    }

    // on below line we are creating an initializing all our
    // variables which we have added in layout file.
    fun bind(note: Note, query: String) {
        binding.note = note

        binding.idTVNote.text =
            if (TextUtils.isEmpty(note.noteTitle)) note.noteDescription else note.noteTitle
        binding.idTVNoteDesc.text = note.noteDescription
        binding.idTVDate.text = DateTimeUtils.getDisplayTime(note.timeStamp)

        setHighLightedText(binding.idTVNote, query)
        setHighLightedText(binding.idTVNoteDesc, query)
    }

    private fun setHighLightedText(tv: TextView, textToHighlight: String) {
        val highlightText = textToHighlight.trim()
        val tvt: String = tv.text.toString().trim()

        var ofe = tvt.indexOf(highlightText, 0)
        val wordToSpan: Spannable = SpannableString(tv.text)
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(highlightText, ofs)
            if (ofe == -1) break else {
                // you can change or add more span as per your need
                val sizeStyle = RelativeSizeSpan(1f)
                val colorStyle = ForegroundColorSpan(Color.RED)

                if (highlightText.isEmpty()) {
                    wordToSpan.removeSpan(sizeStyle)
                    wordToSpan.removeSpan(colorStyle)
                    tv.text = wordToSpan
                } else {
                    wordToSpan.setSpan(
                        sizeStyle, ofe, ofe + highlightText.length, 0
                    ) // set size
                    wordToSpan.setSpan(
                        colorStyle, ofe, ofe + highlightText.length, 0
                    ) // set color
                    tv.setText(wordToSpan, TextView.BufferType.SPANNABLE)
                }
            }
            ofs = ofe + 1
        }
    }

}

class NoteDiffUtil : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.taskId == newItem.taskId
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
//        return (oldItem.noteTitle == newItem.noteTitle)
//                && (oldItem.noteDescription == newItem.noteDescription)
//                && (oldItem.timeStamp == newItem.timeStamp)
        return false
    }

}

interface NoteItemClickInterface {
    fun onItemClick(view: View, position: Int, note: Note)
    fun onItemDeleteClick(position: Int, note: Note)

    // methods for multi selection list
    fun onItemSelectionEnabled(enabled: Boolean)
    fun onItemSelectionChanged(selectionListCounts: Int)
}