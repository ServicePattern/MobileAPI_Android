package com.brightpattern.bpcontactcenter.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContactCenterChatSessionPartyType {
    @SerialName("scenario")
    Scenario,
    @SerialName("external")
    External,
    @SerialName("internal")
    Internal
}