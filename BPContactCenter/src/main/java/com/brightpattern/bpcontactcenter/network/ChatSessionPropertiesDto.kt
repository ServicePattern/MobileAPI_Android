package com.brightpattern.bpcontactcenter.network

import com.brightpattern.bpcontactcenter.entity.FieldName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ChatSessionPropertiesDto(
        @SerialName(FieldName.CHAT_ID)
        val chatID: String,
        val state: ChatSessionStateDto,
        @SerialName(FieldName.EWT)
        val estimatedWaitTime: Int,
        val isNewChat: Boolean,
        val phoneNumber: String,
)

@Serializable
enum class ChatSessionStateDto {
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
