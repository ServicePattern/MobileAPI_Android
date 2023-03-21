package com.brightpattern

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.google.firebase.messaging.FirebaseMessaging

class ChatDemo : Application() {

    companion object {
        var api: ContactCenterCommunicator? = null
        var gcmToken: String? = null
        var chatID: String = ""
        var partyID: String = ""
        var lastMessageID: String = ""

        const val OPEN_MESSAGE_ACTIVITY_REQUEST_ID = 100
        const val OPEN_CALL_ACTIVITY_REQUEST_ID = 100
        const val CLOSED_BY_SERVER = 1001

    }

    private val preferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private val baseURL: String?
        get() {
            return preferences.getString("baseURL", null)
        }

    private val tenantURL: String?
        get() {
            return preferences.getString("tenantURL", null)
        }

    private val appID: String?
        get() {
            return preferences.getString("appID", null)
        }

    private val clientID: String?
        get() {
            return preferences.getString("clientID", null)
        }

    override fun onCreate() {
        super.onCreate()

        registerAPI()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("*****", "Fetching FCM registration token failed", task.exception)
                gcmToken = null
            }
            gcmToken = task.result
        }

    }

    fun registerAPI(): Boolean {
        return ifNotNull(baseURL, tenantURL, appID, clientID) { (baseURL, tenantURL, appID, clientID) ->

            api = ContactCenterCommunicator.init(baseURL as String, tenantURL as String, appID as String, clientID as String, applicationContext)

            return true
        } ?: return false
    }
}