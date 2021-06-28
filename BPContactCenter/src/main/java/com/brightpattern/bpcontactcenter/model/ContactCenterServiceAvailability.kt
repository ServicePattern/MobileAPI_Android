package com.brightpattern.bpcontactcenter.model

import com.brightpattern.bpcontactcenter.entity.FieldName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/// - Tag: ContactCenterServiceChatAvailability
@Serializable
enum class ContactCenterServiceChatAvailability {
    @SerialName("available")
    Available,

    @SerialName("notAvailable")
    Unavailable
}

/// Service status
/// - Tag: ContactCenterServiceAvailability
@Serializable
data class ContactCenterServiceAvailability(
        @SerialName(FieldName.CHAT)
        val chatval: ContactCenterServiceChatAvailability,
        @SerialName(FieldName.EWT)
        val estimatedWaitTime: Int? = null
)
