package com.brightpattern.bpcontactcenter.entity

import com.brightpattern.bpcontactcenter.model.ContactCenterChatSessionState
import com.brightpattern.bpcontactcenter.utils.toList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.time.Instant

@Serializable
sealed class ContactCenterEvent {

    /// Contains a new chat message
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_message")
    data class ChatSessionMessage(
            @SerialName("msg_id") val messageID: String,
            @SerialName("party_id") val partyID: String?,
            @SerialName("msg") val message: String,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Indicates that a message has been delivered
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_message_delivered")
    data class ChatSessionMessageDelivered(
            @SerialName("msg_id") val messageID: String,
            @SerialName("party_id") val partyID: String?,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Indicates that a message has been read
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_message_read")
    data class ChatSessionMessageRead(
            @SerialName("msg_id") val messageID: String,
            @SerialName("party_id") val partyID: String?,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Updates the current state of the chat session. If the state is failed, the client application shall assume that the chat session no longer exists.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_message_status")
    data class ChatSessionStatus(
            val state: ContactCenterChatSessionState,
            @SerialName("ewt") val estimatedWaitTime: String) : ContactCenterEvent()

    /// Indicates that a new party (a new agent) has joined the chat session.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_party_joined")
    data class ChatSessionPartyJoined(
            @SerialName("party_id") val partyID: String,
            @SerialName("first_name") val firstName: String,
            @SerialName("last_name") val lastName: String,
            @SerialName("display_name") val displayName: String,
            val type: ContactCenterChatSessionPartyType,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Indicates that a party has left the chat session.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_party_left")
    data class ChatSessionPartyLeft(
            @SerialName("party_id") val partyID: String,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Indicates that the party started typing a message
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_typing")
    data class ChatSessionTyping(
            @SerialName("party_id") val partyID: String?,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Indicates that the party stopped typing a message
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_not_typing")
    data class ChatSessionNotTyping(
            @SerialName("party_id") val partyID: String?,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Contains a new geographic location
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_location")
    data class ChatSessionLocation(
            @SerialName("party_id") val partyID: String?,
            val url: String?,
            val latitude: Float,
            val longitude: Float,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Indicates that a system has requested an application to display a message. Typically used to display inactivity warning.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_timeout_warning")
    data class ChatSessionTimeoutWarning(
            @SerialName("msg") val message: String,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Indicates that a system has ended the chat session due to the user's inactivity.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_inactivity_timeout")
    data class ChatSessionInactivityTimeout(
            @SerialName("msg") val message: String,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()

    /// Indicates a normal termination of the chat session (e.g., when the chat session is closed by the agent). The client application shall assume that the chat session no longer exists.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_ended")
    data class ChatSessionEnded(
            private val empty: String = "") : ContactCenterEvent()

    /// Client sends the message to end current chat conversation but keep the session open.
    /// Direction: C->S
    @Serializable
    @SerialName("chat_session_disconnect")
    data class ChatSessionDisconnect(
            private val empty: String = "") : ContactCenterEvent()

    /// Client sends the message to end chat session.
    /// Direction: C->S
    @Serializable
    @SerialName("chat_session_end")
    data class ChatSessionEnd(
            private val empty: String = "") : ContactCenterEvent()

    //{"events":[{"event":"chat_session_file","file_id":"6fdadd52-df9e-44bb-af08-6e4cb3b7eb81","file_name":"apktool","file_type":"attachment","msg_id":"4","party_id":"FA713225-A136-4EE4-AF5A-467019A84D67","timestamp":"1614739612"}]}

    @Serializable
    @SerialName("chat_session_file")
    data class ChatSessionFile(
            @SerialName("file_name")
            val fileName: String,
            @SerialName("file_id")
            val fileUUID: String,
            @SerialName("file_type") // TODO: REFACTOR
            val fileType: String,
            @SerialName("party_id")
            val partyID: String,
            val timestamp: Long = Instant.now().toEpochMilli()) : ContactCenterEvent()


    // TODO: Need to refactor
    internal companion object {

        fun listFromJSONEvents(jsonObject: JSONObject, format: Json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            classDiscriminator = "event"
        }): List<ContactCenterEvent> {
            return jsonObject.getJSONArray("events").toList().mapNotNull { fromJson(it, format) }
        }

        fun fromJson(jsonObject: JSONObject, format: Json = Json { isLenient = true }): ContactCenterEvent? {

            return when (jsonObject.getString("event")) {
                "chat_session_message" -> format.decodeFromString(ChatSessionMessage.serializer(), jsonObject.toString())
                "chat_session_message_delivered" -> format.decodeFromString(ChatSessionMessageDelivered.serializer(), jsonObject.toString())
                "chat_session_message_read" -> format.decodeFromString(ChatSessionMessageRead.serializer(), jsonObject.toString())
                "chat_session_status" -> format.decodeFromString(ChatSessionStatus.serializer(), jsonObject.toString())
                "chat_session_party_joined" -> format.decodeFromString(ChatSessionPartyJoined.serializer(), jsonObject.toString())
                "chat_session_party_left" -> format.decodeFromString(ChatSessionPartyLeft.serializer(), jsonObject.toString())
                "chat_session_typing" -> format.decodeFromString(ChatSessionTyping.serializer(), jsonObject.toString())
                "chat_session_not_typing" -> format.decodeFromString(ChatSessionNotTyping.serializer(), jsonObject.toString())
                "chat_session_location" -> format.decodeFromString(ChatSessionLocation.serializer(), jsonObject.toString())
                "chat_session_timeout_warning" -> format.decodeFromString(ChatSessionTimeoutWarning.serializer(), jsonObject.toString())
                "chat_session_inactivity_timeout" -> format.decodeFromString(ChatSessionInactivityTimeout.serializer(), jsonObject.toString())
                "chat_session_ended" -> format.decodeFromString(ChatSessionEnded.serializer(), jsonObject.toString())
                "chat_session_disconnect" -> format.decodeFromString(ChatSessionDisconnect.serializer(), jsonObject.toString())
                "chat_session_end" -> format.decodeFromString(ChatSessionEnd.serializer(), jsonObject.toString())
                "chat_session_file" -> format.decodeFromString(ChatSessionFile.serializer(), jsonObject.toString())
                else -> null
            }
        }
    }
}
