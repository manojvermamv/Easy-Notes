package com.anubhav.takeanote.base

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.takeanote.R

class EmptyViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var tvNotFound: TextView = itemView.findViewById(R.id.not_found_textView)
    var animationView: ImageView = itemView.findViewById(R.id.not_found_animation)

    init {
        tvNotFound.setText(R.string.no_results)
    }

}