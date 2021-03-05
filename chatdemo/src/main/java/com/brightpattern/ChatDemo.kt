package com.brightpattern

import android.app.Application
import android.content.Context
import android.util.Log
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class ChatDemo : Application() {

    companion object {
        lateinit var api: ContactCenterCommunicator
        var gcmToken: String? = null
    }

    val baseURL = "https://alvm.bugfocus.com"
    val tenantURL = "devs.alvm.bugfocus.com"
    val appID = "Android"

    val clientID: String by lazy {
        val default = UUID.randomUUID().toString()
        val preferences = applicationContext.getSharedPreferences("ChatDemo", Context.MODE_PRIVATE)
        preferences.getString("clientID", null)?.let {
            it
        } ?: run {
            preferences.edit().putString("clientID", default).apply()
            default
        }
    }

    override fun onCreate() {
        super.onCreate()

        api = ContactCenterCommunicator.init(baseURL, tenantURL, appID, clientID, applicationContext)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("*****", "Fetching FCM registration token failed", task.exception)
                gcmToken = null
            }
            gcmToken = task.result
        })

    }
}