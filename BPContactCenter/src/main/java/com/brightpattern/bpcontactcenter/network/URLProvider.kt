package com.brightpattern.bpcontactcenter.network

import android.webkit.URLUtil
import com.brightpattern.bpcontactcenter.entity.ContactCenterError

internal class URLProvider {
    companion object {
        const val apiVersion = "v2"
        const val basePath = "clientweb/api"
        var chatID: String = ""
    }

    enum class Endpoint {
        CheckAvailability,
        GetChatHistory,
        GetCaseHistory,
        GetNewChatEvents,
        RequestChat,
        SendEvents,
        SubscribeForNotifications,
        CloseCase;

        private val endpointPathString: String
            get() = when (this) {

                CheckAvailability -> "availability"
                GetChatHistory -> "chats/$chatID/history"
                GetCaseHistory -> "chats/$chatID/casehistory"
                GetNewChatEvents -> "chats/$chatID/events"
                RequestChat -> "chats"
                SendEvents -> "chats/$chatID/events"
                SubscribeForNotifications -> "chats/$chatID/notifications"
                CloseCase -> "chats/$chatID/closecase"
            }

        @Throws(ContactCenterError::class)
        fun generateFullUrl(baseURL: String, tenantURL: String, chatID: String = ""): String {
            URLProvider.chatID = chatID
            val rgx = "^(http|https):\\/\\/".toRegex()
            val result =  "$baseURL/$basePath/$apiVersion/${endpointPathString}?tenantUrl=$tenantURL"
            if (rgx.containsMatchIn(tenantURL))
                throw ContactCenterError.FailedTenantURL("TenantURL MUST NOT starts with http:// or https://")
            if (!URLUtil.isValidUrl(result))
                throw ContactCenterError.FailedToBuildBaseURL("Failed to build URL from baseURL=$baseURL\ttenantURL=$tenantURL\tchatID=$chatID ")
            if(!URLUtil.isHttpsUrl(result))
                throw ContactCenterError.FailedToBuildBaseURL("The URL=$result doesn't contain SSL protocol keyword ")
            return result
        }
    }
}
