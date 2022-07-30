package com.anubhav.easynotes.activities

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.anubhav.commonutility.customfont.FontUtils
import com.anubhav.easynotes.R
import com.anubhav.easynotes.databinding.ActivitySettingsBinding
import com.anubhav.easynotes.fragments.SettingsFragment
import com.anubhav.easynotes.utils.GlobalData
import com.anubhav.easynotes.utils.HelperMethod

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    companion object {
        val TAG: String = SettingsActivity::class.simpleName as String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        //excludeStatusAndNavBarFromTransition(window)
        GlobalData.setStatusBarFullScreen(this)
        setViewHeight(binding.topView)

        FontUtils.setFont(this, binding.root as ViewGroup)

        onCreateActionBar()
        if (savedInstanceState == null) {
            // below line is to inflate our fragment.
            supportFragmentManager.beginTransaction().add(R.id.frame_layout, SettingsFragment())
                .commit()
        }
    }

    private fun onCreateActionBar() {
        binding.layActionbar.actionBarTitle = getString(R.string.app_name)
        binding.layActionbar.menuVisible = false

        binding.layActionbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun showKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    private fun setViewHeight(view: View) {
        val params: ViewGroup.LayoutParams = view.layoutParams
        params.height = HelperMethod.getStatusBarHeight(this)
        view.layoutParams = params
    }

}