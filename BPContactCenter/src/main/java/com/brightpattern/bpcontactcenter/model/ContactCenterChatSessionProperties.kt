package com.brightpattern.bpcontactcenter.model

import com.brightpattern.bpcontactcenter.entity.FieldName
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
        @SerialName(FieldName.CHAT_ID)
        val chatID: String,
        @SerialName(FieldName.STATE)
        val state: ContactCenterChatSessionState,
        @SerialName(FieldName.EWT)
        val estimatedWaitTime: Int,
        @SerialName(FieldName.IS_NEW_CHAT)
        val isNewChat: Boolean,
        @SerialName(FieldName.PHONE_NUMBER)
        val phoneNumber: String = "Undefined"
)
