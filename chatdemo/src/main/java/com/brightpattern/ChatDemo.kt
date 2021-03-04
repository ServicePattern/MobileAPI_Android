package com.brightpattern

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class ChatDemo: Application() {

    companion object {
        lateinit var api: ContactCenterCommunicator
    }

    val baseURL = "https://alvm.bugfocus.com"
    val tenantURL = "devs.alvm.bugfocus.com"
    val clientID = UUID.randomUUID().toString()
    val appID = "Android"

    override fun onCreate() {
        super.onCreate()

        api = ContactCenterCommunicator.init(baseURL, tenantURL, appID, clientID, applicationContext)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("*****", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = token //getString(R.string.msg_token_fmt, token)
            Log.d("******", "$msg")
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }
}