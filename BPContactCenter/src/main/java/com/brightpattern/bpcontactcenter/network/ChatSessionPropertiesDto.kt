package com.brightpattern.bpcontactcenter.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ChatSessionPropertiesDto(
        @SerialName("chat_id")
        val chatID: String,
        val state: ChatSessionStateDto,
        @SerialName("ewt")
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

//{"channel":"web","event":"chat_session_message","msg":"Please wait for the next available agent","msg_id":"0","party_id":"7215D0DA-D272-499C-9CF3-9F1A322F42CB","timestamp":"1614718748"}
//{"event":"chat_session_status","ewt":"","state":"queued"}
//{"display_name":"Artem Mkrtchyan","event":"chat_session_party_joined","first_name":"Artem","last_name":"Mkrtchyan","party_id":"80A8248D-69D7-4DA2-8074-6139AA191D7E","timestamp":"1614718750","type":"internal"}