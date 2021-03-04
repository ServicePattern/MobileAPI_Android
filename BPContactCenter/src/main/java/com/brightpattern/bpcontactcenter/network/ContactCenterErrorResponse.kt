package com.brightpattern.bpcontactcenter.network

import com.brightpattern.bpcontactcenter.entity.ContactCenterError

data class ContactCenterErrorResponse(
        val error_code: String,
        val error_message: String
) {

    fun toModel(): ContactCenterError? {
        return if (error_code == "5005") {
            ContactCenterError.chatSessionNotFound()
        } else null
    }
}