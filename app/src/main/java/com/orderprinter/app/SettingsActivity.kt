package com.orderprinter.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.orderprinter.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            // Εμφάνιση τιμών στο summary
            val keys = listOf("printer_email", "sender_email", "smtp_host", "smtp_port", "discount_percent", "vat_percent")
            for (key in keys) {
                val pref = findPreference<EditTextPreference>(key)
                pref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            }

            // Κρύψε τον κωδικό στο summary
            val passwordPref = findPreference<EditTextPreference>("sender_password")
            passwordPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }
    }
}
