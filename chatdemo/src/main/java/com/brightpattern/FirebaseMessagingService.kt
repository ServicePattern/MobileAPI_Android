package com.brightpattern

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CDFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FBMessageService", ">> ${remoteMessage.data}")
        if (remoteMessage.data.containsKey("chatID"))
            ChatDemo.api.appDidReceiveMessage(remoteMessage.data.toMap())
    }

    override fun onNewToken(token: String) {
        // TODO: handle it!
        Log.e("FBMessageService", "Refreshed token: $token")
    }
}