package com.brightpattern.bpcontactcenter.model

import com.brightpattern.bpcontactcenter.entity.FieldName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/// Service status
/// - Tag: ContactCenterServiceAvailability
@Serializable
data class ContactCenterVersion(
        @SerialName(FieldName.SERVER_VERSION)
        val serverVersion: String
)
