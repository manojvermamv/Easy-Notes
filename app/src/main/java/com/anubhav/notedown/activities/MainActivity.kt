package com.anubhav.notedown.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.notedown.R
import com.anubhav.notedown.adapters.MainActionsAdapter
import com.anubhav.notedown.adapters.tabIconRes
import com.anubhav.notedown.database.AppDatabase
import com.anubhav.notedown.database.model.Note
import com.anubhav.notedown.database.model.NoteDao
import com.anubhav.notedown.databinding.ActivityMainBinding
import com.anubhav.notedown.fragments.MainNotesFragment
import com.anubhav.notedown.utils.GlobalData
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.OnCompleteListener
import com.sanojpunchihewa.updatemanager.UpdateManager
import com.sanojpunchihewa.updatemanager.UpdateManagerConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding
    lateinit var reviewManager: ReviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //excludeStatusAndNavBarFromTransition(window)

        onCreateActionBar()
        onCreateApp()
    }

    private fun onCreateActionBar() {
        binding.imgMenu.setOnClickListener(View.OnClickListener {
            binding.rootView.openDrawer(GravityCompat.START)
        })

        binding.imgSettings.setOnClickListener(View.OnClickListener { })
        setSelectionActionBar(false)
    }

    private fun onCreateApp() {
        binding.navView.setNavigationItemSelectedListener(this)

        val navigationHeader = binding.navView.getHeaderView(0) as ViewGroup
        FontUtils.setFont(this, navigationHeader)

        // init app review and app update
        reviewManager = ReviewManagerFactory.create(this)
        val updateManager = UpdateManager.Builder(this).mode(UpdateManagerConstant.FLEXIBLE);
        updateManager.start()

        // setup viewPager with tab layout
        initViewPager()
    }

    private fun initViewPager() {
        val adapter = MainActionsAdapter(this, supportFragmentManager)
        binding.viewPager.setSwipeable(false)
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager, true)
        for (i in 0 until binding.tabLayout.tabCount) {
            val view: View = layoutInflater.inflate(R.layout.custom_tab_icon, null)
            view.findViewById<ImageView>(R.id.icon).setImageResource(tabIconRes[i])
            val tab = binding.tabLayout.getTabAt(i) as TabLayout.Tab
            tab.customView = view;
            if (i == 1) tab.view.isEnabled = false
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab == null) return
                val view = tab.customView as View
                val imgIcon = view.findViewById<ImageView>(R.id.icon)
                imgIcon.isSelected = true
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                if (tab == null) return
                val view = tab.customView as View
                val imgIcon = view.findViewById<ImageView>(R.id.icon)
                imgIcon.isSelected = false
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        binding.viewPager.currentItem = 0
    }

    fun setSelectionActionBar(enable: Boolean) {
        if (enable) {
            binding.layTop.visibility = View.GONE
            binding.layTopSelection.visibility = View.VISIBLE
        } else {
            binding.layTop.visibility = View.VISIBLE
            binding.layTopSelection.visibility = View.GONE
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.rootView.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_favorites -> {
                startActivity(Intent(this, FavoriteNotesActivity::class.java))
            }
            R.id.nav_other_apps -> GlobalData.openAppStoreDeveloper(this)
            R.id.nav_rate_app -> showRateApp()
            R.id.nav_about -> GlobalData.openAppStoreDeveloper(this)
        }
        return false
    }

    override fun onBackPressed() {
        if (binding.rootView.isDrawerOpen(GravityCompat.START)) {
            binding.rootView.closeDrawer(GravityCompat.START)
        } else if (binding.layTopSelection.visibility == View.VISIBLE) {
            binding.imgSelectionClose.performClick()
        } else {
            super.onBackPressed()
        }
    }

    private fun resetNoteState(context: Context) = CoroutineScope(context = Dispatchers.IO).launch {
        val noteDao = AppDatabase.getDatabase(context).getNoteDao()
        val list = noteDao.getAllNotes().value as List<Note>
        list.forEach {
            it.searchQuery = ""
            it.isSelected = false
        }
        noteDao.insertItemList(list)
    }

    override fun onDestroy() {
        val fragment: Fragment? = supportFragmentManager.findFragmentByTag(
            "android:switcher:" + R.id.viewPager.toString() + ":" + binding.viewPager.currentItem
        )
        super.onDestroy()
    }

    // Shows the app rate dialog box using In-App review API
    // The app rate dialog box might or might not shown depending
    // on the Quotas and limitations
    private fun showRateApp() {
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener(OnCompleteListener {
            if (it.isSuccessful) {
                val taskFlow = reviewManager.launchReviewFlow(this, it.result)
                taskFlow.addOnCompleteListener(OnCompleteListener {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                })
            }
        })
    }

}

fun excludeStatusAndNavBarFromTransition(window: Window) {
    val fade = Fade()
    fade.excludeTarget(android.R.id.statusBarBackground, true)
    fade.excludeTarget(android.R.id.navigationBarBackground, true)
    window.enterTransition = fade
    window.exitTransition = fade
}