package com.brightpattern.bpcontactcenter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/// - Tag: ContactCenterServiceChatAvailability
@Serializable
enum class ContactCenterServiceChatAvailability {
    @SerialName("available")
    Available,
    @SerialName("unavailable")
    Unavailable
}

/// Service status
/// - Tag: ContactCenterServiceAvailability
@Serializable
data class ContactCenterServiceAvailability(
        @SerialName("chat")
        val chatval: ContactCenterServiceChatAvailability,
        @SerialName("ewt")
        val estimatedWaitTime: Int,
)
