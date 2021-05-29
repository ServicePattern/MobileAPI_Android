package com.brightpattern.bpcontactcenter.network

import android.webkit.URLUtil
import com.brightpattern.bpcontactcenter.entity.ContactCenterError

internal class URLProvider {
    companion object {
        const val apiVersion = "v2"
        const val basePath = "clientweb/api"
        const val filePath = "clientweb/file"
        var chatID: String = ""
        var fileID: String = ""
    }

    enum class Endpoint {
        Version,
        CheckAvailability,
        GetChatHistory,
        GetCaseHistory,
        GetNewChatEvents,
        RequestChat,
        SendEvents,
        SubscribeForNotifications,
        CloseCase,
        File;

        private val endpointPathString: String
            get() = when (this) {

                Version -> "version"
                CheckAvailability -> "availability"
                GetChatHistory -> "chats/$chatID/history"
                GetCaseHistory -> "chats/$chatID/casehistory"
                GetNewChatEvents -> "chats/$chatID/events"
                RequestChat -> "chats"
                SendEvents -> "chats/$chatID/events"
                SubscribeForNotifications -> "chats/$chatID/notifications"
                CloseCase -> "chats/$chatID/closecase"
                File -> "$fileID"
            }

        @Throws(ContactCenterError::class)
        fun generateFullUrl(baseURL: String, tenantURL: String, chatID: String = ""): String {
            URLProvider.chatID = chatID
            val rgx = "^(http|https):\\/\\/".toRegex()
            val nonce = System.currentTimeMillis().toString()
            val result =  "$baseURL/$basePath/$apiVersion/${endpointPathString}?tenantUrl=$tenantURL&nonce=$nonce"
            if (rgx.containsMatchIn(tenantURL))
                throw ContactCenterError.FailedTenantURL("TenantURL MUST NOT starts with http:// or https://")
            if (!URLUtil.isValidUrl(result))
                throw ContactCenterError.FailedToBuildBaseURL("Failed to build URL from baseURL=$baseURL\ttenantURL=$tenantURL\tchatID=$chatID ")
            if(!URLUtil.isHttpsUrl(result))
                throw ContactCenterError.FailedToBuildBaseURL("The URL=$result doesn't contain SSL protocol keyword ")
            return result
        }

        @Throws(ContactCenterError::class)
        fun generateFileUrl(baseURL: String, fileID: String): String {
            URLProvider.fileID = fileID
            val rgx = "^(http|https):\\/\\/".toRegex()
            val nonce = System.currentTimeMillis().toString()
            val result =  "$baseURL/$filePath/${endpointPathString}"
            if (!URLUtil.isValidUrl(result))
                throw ContactCenterError.FailedToBuildBaseURL("Failed to build file URL from baseURL=$baseURL\tfileID=$fileID ")
            if(!URLUtil.isHttpsUrl(result))
                throw ContactCenterError.FailedToBuildBaseURL("The URL=$result doesn't contain SSL protocol keyword ")
            return result
        }
    }
}
