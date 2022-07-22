package com.anubhav.notedown.interfaces

interface RvItemClickListener<T> {

    fun onItemClick(position: Int, item: T)

    fun onItemLongClick(position: Int, item: T)

    fun onItemSelected(position: Int, item: T)

    fun onItemsSelected(selectedItems: List<T>?)

    fun onItemSelectionChanged(position: Int, item: T)

}