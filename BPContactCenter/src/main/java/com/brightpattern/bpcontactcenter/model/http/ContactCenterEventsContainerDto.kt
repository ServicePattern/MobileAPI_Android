package com.brightpattern.bpcontactcenter.model.http

import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import kotlinx.serialization.Serializable

@Serializable
data class ContactCenterEventsContainerDto(val events: List<ContactCenterEvent>)