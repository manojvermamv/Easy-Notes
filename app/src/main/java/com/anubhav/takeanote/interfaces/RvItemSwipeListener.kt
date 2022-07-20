package com.anubhav.takeanote.interfaces

import androidx.recyclerview.widget.RecyclerView

interface RvItemSwipeListener {
    fun onItemLeftSwipe(viewHolder: RecyclerView.ViewHolder, position: Int)
    fun onItemRightSwipe(viewHolder: RecyclerView.ViewHolder, position: Int)
}