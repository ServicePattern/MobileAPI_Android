package com.brightpattern.bpcontactcenter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContactCenterChatSessionState {
    @SerialName("queued")
    Queued,
    @SerialName("connecting")
    Connecting,
    @SerialName("connected")
    Connected,
    @SerialName("ivr")
    Ivr,
    @SerialName("failed")
    Failed,
    @SerialName("completed")
    Completed
}

@Serializable
data class ContactCenterChatSessionProperties(
        @SerialName("chat_id")
        val chatID: String,
        @SerialName("state")
        val state: ContactCenterChatSessionState,
        @SerialName("ewt")
        val estimatedWaitTime: Int,
        @SerialName("is_new_chat")
        val isNewChat: Boolean,
        @SerialName("phone_number")
        val phoneNumber: String = "Undefined"
)
