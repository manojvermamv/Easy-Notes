package com.anubhav.takeanote.adapters

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.takeanote.BR
import com.anubhav.takeanote.R
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.utils.DateTimeUtils
import java.util.*

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

class BindViewHolder(
    private val binding: ViewDataBinding,
    private val tvTitle: TextView = binding.root.findViewById(R.id.idTVNote),
    private val tvDesc: TextView = binding.root.findViewById(R.id.idTVNoteDesc),
    private val tvDate: TextView = binding.root.findViewById(R.id.idTVDate)
) : RecyclerView.ViewHolder(binding.root) {
    // on below line we are creating an initializing all our
    // variables which we have added in layout file.
    fun bind(note: Note, noteItemClickInterface: NoteItemClickInterface) {
        binding.setVariable(BR.note, note)
        binding.setVariable(BR.onNoteItemClick, noteItemClickInterface)

        tvTitle.text =
            if (TextUtils.isEmpty(note.noteTitle)) note.noteDescription else note.noteTitle
        tvDesc.text = note.noteDescription
        tvDate.text = DateTimeUtils.getDisplayTime(note.timeStamp)

        setHighLightedText(tvTitle, note.searchQuery)
        setHighLightedText(tvDesc, note.searchQuery)
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
    fun onNoteClick(view: View, note: Note);
    fun onNoteDeleteClick(note: Note)
}