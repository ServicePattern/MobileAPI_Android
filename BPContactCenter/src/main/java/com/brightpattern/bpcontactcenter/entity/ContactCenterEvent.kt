package com.brightpattern.bpcontactcenter.entity

import com.brightpattern.bpcontactcenter.model.ContactCenterChatSessionState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed class ContactCenterEvent {

    /// Contains a new chat message
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_message")
    data class ChatSessionMessage(
            @SerialName(FieldName.MSG_ID) val messageID: String = "",
            @SerialName(FieldName.PARTY_ID) val partyID: String?,
            @SerialName(FieldName.MSG) val message: String,
            val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    /// Indicates that a message has been delivered
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_message_delivered")
    data class ChatSessionMessageDelivered(
            @SerialName(FieldName.REF_MSG_ID) val messageID: String,
            @SerialName(FieldName.PARTY_ID) val partyID: String?,
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    /// Indicates that a message has been read
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_message_read")
    data class ChatSessionMessageRead(
            @SerialName(FieldName.REF_MSG_ID) val messageID: String,
            @SerialName(FieldName.PARTY_ID) val partyID: String?,
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    /// Updates the current state of the chat session. If the state is failed, the client application shall assume that the chat session no longer exists.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_message_status")
    data class ChatSessionMessageStatus(
            val state: ContactCenterChatSessionState,
            @SerialName(FieldName.EWT) val estimatedWaitTime: String) : ContactCenterEvent()

    /// Indicates that a new party (a new agent) has joined the chat session.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_party_joined")
    data class ChatSessionPartyJoined(
            @SerialName(FieldName.PARTY_ID) val partyID: String = "",
            @SerialName(FieldName.FIRST_NAME) val firstName: String = "",
            @SerialName(FieldName.LAST_NAME) val lastName: String = "",
            @SerialName(FieldName.DISPLAY_NAME) val displayName: String = "",
            val type: ContactCenterChatSessionPartyType,
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    /// Indicates that a party has left the chat session.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_party_left")
    data class ChatSessionPartyLeft(
            @SerialName(FieldName.PARTY_ID) val partyID: String,
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    /// Indicates that the party started typing a message
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_typing")
    data class ChatSessionTyping(
            @SerialName(FieldName.PARTY_ID) val partyID: String?,
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    /// Indicates that the party stopped typing a message
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_not_typing")
    data class ChatSessionNotTyping(
            @SerialName(FieldName.PARTY_ID) val partyID: String?,
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    /// Contains a new geographic location
    /// Direction: S<->C
    @Serializable
    @SerialName("chat_session_location")
    data class ChatSessionLocation(
            @SerialName(FieldName.PARTY_ID) val partyID: String?,
            val url: String?,
            val latitude: Float,
            val longitude: Float,
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    /// Indicates that a system has requested an application to display a message. Typically used to display inactivity warning.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_timeout_warning")
    data class ChatSessionTimeoutWarning(
            @SerialName(FieldName.MSG) val message: String,
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    /// Indicates that a system has ended the chat session due to the user's inactivity.
    /// Direction: S->C
    @Serializable
    @SerialName("chat_session_inactivity_timeout")
    data class ChatSessionInactivityTimeout(
            @SerialName(FieldName.MSG) val message: String,
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

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

    @Serializable
    @SerialName("chat_session_file")
    data class ChatSessionFile(
            @SerialName(FieldName.FILE_NAME)
            val fileName: String,
            @SerialName(FieldName.FILE_ID)
            val fileUUID: String,
            @SerialName(FieldName.FILE_TYPE)
            val fileType: String,
            @SerialName(FieldName.PARTY_ID)
            val partyID: String,
            var url: String? = null,
            val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    @Serializable
    @SerialName("chat_session_form_submit")
    data class ChatSessionFormSubmit(
            @SerialName(FieldName.FORM_DATA)
            val formData: Map<String, String> = mapOf(),
            @SerialName(FieldName.PARTY_ID)
            val partyID: String?,
            @SerialName(FieldName.REQ_ID)
            val reqID: Int = -1,
            @SerialName(FieldName.TIMESTAMP)
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    @Serializable
    @SerialName("chat_session_case_set")
    data class ChatSessionCaseSet(
            @SerialName(FieldName.CASE_ID)
            val caseID: String,
            @SerialName(FieldName.TIMESTAMP)
             val timestamp: Long = System.currentTimeMillis() / 1000) : ContactCenterEvent()

    @Serializable
    @SerialName("chat_session_status")
    data class ChatSessionStatus(
            @SerialName(FieldName.EWT) val estimatedWaitTime: String
    ) : ContactCenterEvent()

    @Serializable
    @SerialName("chat_session_unknown_event")
    data class ChatSessionUnknownEvent(
            val errorDescription: String = "API received unknown event entry"
    ) : ContactCenterEvent()

    @Serializable
    @SerialName("chat_session_signaling")
    data class ChatSessionSignaling(
        @SerialName("data") val data: SignalingData,
        @SerialName(FieldName.PARTY_ID)
        val party_id: String,
        @SerialName(FieldName.TIMESTAMP)
        val timestamp: String
    ) :ContactCenterEvent()

    @Serializable
    data class SignalingData(
        val candidate: String? = null,
        val sdpMLineIndex: String? = null,
        val sdpMid: String? = null,
        val type: String? = null
    )

    @Serializable
    @SerialName("chat_session_form_show")
    data class ChatSessionFormShow(
        @SerialName("channel")
        val channel:String,
        @SerialName("form_name")
        val formName: String,
        @SerialName("form_request_id")
        val requestID: Int,
        @SerialName("form_timeout")
        val timeout: Int,
        @SerialName(FieldName.TIMESTAMP)
        val timestamp: String
    ) : ContactCenterEvent ()

    // TODO: need to implement
    companion object {
        val jsonSerializer = SerializersModule {
            polymorphic(ContactCenterEvent::class) {
                subclass(ChatSessionMessage::class)
                subclass(ChatSessionMessageDelivered::class)
                subclass(ChatSessionMessageRead::class)
                subclass(ChatSessionMessageStatus::class)

                subclass(ChatSessionPartyJoined::class)
                subclass(ChatSessionPartyLeft::class)

                subclass(ChatSessionTyping::class)
                subclass(ChatSessionNotTyping::class)
                subclass(ChatSessionLocation::class)
                subclass(ChatSessionTimeoutWarning::class)

                subclass(ChatSessionInactivityTimeout::class)
                subclass(ChatSessionEnded::class)
                subclass(ChatSessionDisconnect::class)
                subclass(ChatSessionEnd::class)

                subclass(ChatSessionFile::class)
                subclass(ChatSessionFormSubmit::class)
                subclass(ChatSessionCaseSet::class)
                subclass(ChatSessionStatus::class)

                subclass(ChatSessionSignaling::class)

                default { ChatSessionUnknownEvent.serializer() }

            }
        }
    }
}
