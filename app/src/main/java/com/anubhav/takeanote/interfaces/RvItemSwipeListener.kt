package com.anubhav.takeanote.interfaces

interface RvItemSwipeListener {
    fun onItemDelete(position: Int)
    fun onItemFavorite(position: Int)
}