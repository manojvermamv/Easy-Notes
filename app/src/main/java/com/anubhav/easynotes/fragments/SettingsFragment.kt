package com.anubhav.easynotes.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.anubhav.easynotes.R
import com.anubhav.easynotes.utils.GlobalData

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        // below line is used to add preference
        // fragment from our xml folder.
        addPreferencesFromResource(R.xml.settings_prefs);

        setPreferencesClick(getString(R.string.pref_privacy_policy))
        setPreferencesClick(getString(R.string.pref_aboutus))
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.pref_privacy_policy) -> GlobalData.openAppPrivacyPolicy(requireContext())
            getString(R.string.pref_aboutus) -> GlobalData.openAppStoreDeveloper(requireContext())
        }
        return true
    }

    private fun setPreferencesClick(key: String) {
        val prefs: Preference? = findPreference(key)
        prefs?.onPreferenceClickListener = this
    }

}