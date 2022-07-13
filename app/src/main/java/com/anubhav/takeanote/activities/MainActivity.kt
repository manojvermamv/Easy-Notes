package com.anubhav.takeanote.activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.takeanote.R
import com.anubhav.takeanote.adapters.MainActionsAdapter
import com.anubhav.takeanote.adapters.tabIconRes
import com.anubhav.takeanote.databinding.ActivityMainBinding
import com.anubhav.takeanote.utils.GlobalData
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.OnCompleteListener
import com.sanojpunchihewa.updatemanager.UpdateManager
import com.sanojpunchihewa.updatemanager.UpdateManagerConstant


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding
    lateinit var reviewManager: ReviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        onCreateActionBar()
        onCreateApp()
    }

    private fun onCreateActionBar() {
        binding.imgMenu.setOnClickListener(View.OnClickListener {
            binding.rootView.openDrawer(GravityCompat.START)
        })

        binding.imgSettings.setOnClickListener(View.OnClickListener { })
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
        val adapter = MainActionsAdapter(this, supportFragmentManager)
        binding.viewPager.setSwipeable(false)
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager, true);
        for (i in 0 until binding.tabLayout.tabCount) {
            val view: View = layoutInflater.inflate(R.layout.custom_tab_icon, null)
            view.findViewById<ImageView>(R.id.icon).setImageResource(tabIconRes[i]);
            val tab = binding.tabLayout.getTabAt(i) as TabLayout.Tab
            tab.customView = view;
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_favorites -> {
            }
            R.id.nav_other_apps -> GlobalData.openAppStoreDeveloper(this)
            R.id.nav_rate_app -> showRateApp()
            R.id.nav_about -> GlobalData.openAppStoreDeveloper(this)
        }
        return true
    }

    override fun onBackPressed() {
        if (binding.rootView.isDrawerOpen(GravityCompat.START)) {
            binding.rootView.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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