package com.anubhav.takeanote.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.anubhav.takeanote.R
import com.anubhav.takeanote.fragments.MainNotesFragment
import com.anubhav.takeanote.fragments.MainTasksFragment

val tabIconRes = intArrayOf(
    R.drawable.ic_notes,
    R.drawable.ic_tasks
)

class MainActionsAdapter(val context: Context, private val fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {

    private val NOTES = 0
    private val TASKS = 1

    private val tabs = intArrayOf(NOTES, TASKS)

    override fun getItem(position: Int): Fragment {
        return when (tabs[position]) {
            NOTES -> MainNotesFragment.newInstance("")
            TASKS -> MainTasksFragment.newInstance("")
            else -> Fragment()
        }
    }

    override fun getCount(): Int {
        return tabs.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (tabs[position]) {
            NOTES -> ""
            TASKS -> ""
            else -> ""
        }
    }

}