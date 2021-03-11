package com.brightpattern

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.brightpattern.chatdemo.R

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pref)

        supportFragmentManager.beginTransaction().replace(R.id.container, PrefFragment()).commit()
    }

    override fun onBackPressed() {
        (applicationContext as ChatDemo).registerAPI() // ugly but fast, do not repeat !!!
        super.onBackPressed()
    }

    class PrefFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }
    }
}

