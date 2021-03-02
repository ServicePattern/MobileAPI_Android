package com.brightpattern.bpcontactcenter.entity

import com.brightpattern.bpcontactcenter.model.ContactCenterChatSessionState
import java.time.Instant

sealed class ContactCenterEvent {
    /// Contains a new chat message
    /// Direction: S<->C
    data class ChatSessionMessage(val messageID: String, val partyID: String?, val message: String, val timestamp: Instant?): ContactCenterEvent()
    /// Indicates that a message has been delivered
    /// Direction: S<->C
    data class ChatSessionMessageDelivered(val messageID: String, val partyID: String?, val timestamp: Instant?): ContactCenterEvent()
    /// Indicates that a message has been read
    /// Direction: S<->C
    data class ChatSessionMessageRead(val messageID: String, val partyID: String?, val timestamp: Instant?): ContactCenterEvent()
    /// Updates the current state of the chat session. If the state is failed, the client application shall assume that the chat session no longer exists.
    /// Direction: S->C
    data class ChatSessionStatus(val state: ContactCenterChatSessionState, val estimatedWaitTime: Int): ContactCenterEvent()
    /// Indicates that a new party (a new agent) has joined the chat session.
    /// Direction: S->C
    data class ChatSessionPartyJoined(val partyID: String, val firstName: String, val lastName: String,val  displayName: String, val type: ContactCenterChatSessionPartyType, val timestamp: Instant): ContactCenterEvent()
    /// Indicates that a party has left the chat session.
    /// Direction: S->C
    data class ChatSessionPartyLeft(val partyID: String, val timestamp: Instant): ContactCenterEvent()
    /// Indicates that the party started typing a message
    /// Direction: S<->C
    data class ChatSessionTyping(val partyID: String?, val timestamp: Instant?): ContactCenterEvent()
    /// Indicates that the party stopped typing a message
    /// Direction: S<->C
    data class ChatSessionNotTyping(val partyID: String?, val timestamp: Instant?): ContactCenterEvent()
    /// Contains a new geographic location
    /// Direction: S<->C
    data class ChatSessionLocation(val partyID: String?, val url: String?, val latitude: Float, val longitude: Float, val timestamp: Instant?): ContactCenterEvent()
    /// Indicates that a system has requested an application to display a message. Typically used to display inactivity warning.
    /// Direction: S->C
    data class ChatSessionTimeoutWarning(val message: String, val timestamp: Instant): ContactCenterEvent()
    /// Indicates that a system has ended the chat session due to the user's inactivity.
    /// Direction: S->C
    data class ChatSessionInactivityTimeout(val message: String, val timestamp: Instant): ContactCenterEvent()
    /// Indicates a normal termination of the chat session (e.g., when the chat session is closed by the agent). The client application shall assume that the chat session no longer exists.
    /// Direction: S->C
    data class ChatSessionEnded(private val empty: String = "") : ContactCenterEvent()
    /// Client sends the message to end current chat conversation but keep the session open.
    /// Direction: C->S
    data class ChatSessionDisconnect(private val empty: String = ""): ContactCenterEvent()
    /// Client sends the message to end chat session.
    /// Direction: C->S
    data class ChatSessionEnd(private val empty: String = ""): ContactCenterEvent()
}
