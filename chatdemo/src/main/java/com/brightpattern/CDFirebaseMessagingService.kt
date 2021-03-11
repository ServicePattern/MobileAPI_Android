package com.brightpattern

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CDFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FBMessageService", ">> ${remoteMessage.data}")
        if (remoteMessage.data.containsKey("chatID"))
            ChatDemo.api?.appDidReceiveMessage(remoteMessage.data.toMap())
    }

    override fun onNewToken(token: String) {
        Log.e("FBMessageService", "Refreshed token: $token")
        ifNotNull(token.isNotEmpty(), ChatDemo.chatID) {
            ChatDemo.api?.subscribeForRemoteNotificationsFirebase(token, ChatDemo.chatID) {
                Log.e("onNewToken", " subscribeForRemoteNotificationsFirebase > $it")
            }
        }
    }
}