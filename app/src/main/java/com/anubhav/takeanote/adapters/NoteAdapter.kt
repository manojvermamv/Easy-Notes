package com.anubhav.takeanote.adapters

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
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.takeanote.BR
import com.anubhav.takeanote.R
import com.anubhav.takeanote.base.EmptyViewHolder
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.utils.DateTimeUtils
import java.util.*

class NoteAdapter(
    private val context: Context,
    private val noteItemClickInterface: NoteItemClickInterface
) :
    ListAdapter<Note, RecyclerView.ViewHolder>(NoteDiffUtil()) {

    private val viewTypeToLayoutId: MutableMap<Int, Int> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            viewTypeToLayoutId[viewType] ?: 0,
            parent,
            false
        )

        // set font style
        FontUtils.setFont(context, binding.root as ViewGroup)

        // set view holder according viewType
        if (viewType == R.layout.item_not_found) {
            return EmptyViewHolder(binding.root)
        }
        return BindViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BindViewHolder) {
            holder.bind(position, getItem(position), noteItemClickInterface, selectionMode)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (!viewTypeToLayoutId.containsKey(item.viewType)) {
            viewTypeToLayoutId[item.viewType] = item.layoutId
        }
        return item.viewType
    }

    var selectionMode: Boolean
        get() {
            return false
        }
        set(v) {}

}

class BindViewHolder(
    private val binding: ViewDataBinding,
    private val rootView: ViewGroup = binding.root.findViewById(R.id.root_view),
    private val tvTitle: TextView = binding.root.findViewById(R.id.idTVNote),
    private val tvDesc: TextView = binding.root.findViewById(R.id.idTVNoteDesc),
    private val tvDate: TextView = binding.root.findViewById(R.id.idTVDate),
    private val ivSelection: ImageView = binding.root.findViewById(R.id.iv_selection)
) : RecyclerView.ViewHolder(binding.root) {
    // on below line we are creating an initializing all our
    // variables which we have added in layout file.
    fun bind(
        position: Int,
        note: Note,
        noteItemClickInterface: NoteItemClickInterface,
        selectionMode: Boolean
    ) {
        binding.setVariable(BR.position, position)
        binding.setVariable(BR.note, note)
        binding.setVariable(BR.onNoteItemClick, noteItemClickInterface)

        tvTitle.text =
            if (TextUtils.isEmpty(note.noteTitle)) note.noteDescription else note.noteTitle
        tvDesc.text = note.noteDescription
        tvDate.text = DateTimeUtils.getDisplayTime(note.timeStamp)

        setHighLightedText(tvTitle, note.searchQuery)
        setHighLightedText(tvDesc, note.searchQuery)

        rootView.setOnClickListener {
            noteItemClickInterface.onItemClick(rootView, position, note)
        }

        rootView.setOnLongClickListener {
            noteItemClickInterface.onItemLongClick(rootView, position, note)
            return@setOnLongClickListener true
        }

        if (selectionMode) {
            ivSelection.visibility = View.VISIBLE
            ivSelection.isSelected = note.isSelected
            ivSelection.setOnClickListener {
                noteItemClickInterface.onItemSelectionClick(ivSelection, position, note)
            }
        } else {
            ivSelection.visibility = View.GONE
        }
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
                        sizeStyle,
                        ofe,
                        ofe + highlightText.length,
                        0
                    ) // set size
                    wordToSpan.setSpan(
                        colorStyle,
                        ofe,
                        ofe + highlightText.length,
                        0
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
        return (oldItem.noteTitle == newItem.noteTitle)
                && (oldItem.noteDescription == newItem.noteDescription)
                && (oldItem.timeStamp == newItem.timeStamp)
                && (oldItem.searchQuery == newItem.searchQuery)
                && (oldItem.searchQuery != newItem.searchQuery)
    }

}

interface NoteItemClickInterface {
    fun onItemClick(view: View, position: Int, note: Note);
    fun onItemLongClick(view: View, position: Int, note: Note);
    fun onItemSelectionClick(view: View, position: Int, note: Note);
    fun onItemDeleteClick(position: Int, note: Note)
}