package com.brightpattern.bpcontactcenter.network

import java.net.URL

internal class URLProvider {
    companion object {
        const val apiVersion = "v2"
        const val basePath = "clientweb/api"
        var chatID: String = ""
    }

    enum class Endpoint {
        CheckAvailability,
        GetChatHistory,
        GetNewChatEvents,
        RequestChat,
        SendEvents,
        SubscribeForNotifications;

        private val endpointPathString: String
            get() = when (this) {

                CheckAvailability -> "availability"
                GetChatHistory -> "chats/$chatID/history"
                GetNewChatEvents -> "chats/$chatID/events"
                RequestChat -> "chats"
                SendEvents -> "chats/$chatID/events"
                SubscribeForNotifications -> "chats/$chatID/notifications"

            }

        fun generateFullUrl(baseURL: String, tenantURL: String, chatID: String = ""): String {
            URLProvider.chatID = chatID
            return "$baseURL/$basePath/$apiVersion/${endpointPathString}?tenantUrl=$tenantURL"
        }
    }
}
