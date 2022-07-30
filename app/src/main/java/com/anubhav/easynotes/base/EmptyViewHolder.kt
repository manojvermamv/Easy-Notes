package com.anubhav.easynotes.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.easynotes.R
import com.anubhav.easynotes.databinding.ItemNotFoundBinding

class EmptyViewHolder(context: Context, binding: ItemNotFoundBinding) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        // set font style
        FontUtils.setFont(context, binding.root as ViewGroup)

        binding.rootView.visibility = View.VISIBLE
        binding.notFoundTextView.setText(R.string.no_results_notes)
    }

}