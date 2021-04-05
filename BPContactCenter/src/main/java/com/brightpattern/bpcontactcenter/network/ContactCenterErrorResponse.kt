package com.brightpattern.bpcontactcenter.network

import com.brightpattern.bpcontactcenter.entity.ContactCenterError

data class ContactCenterErrorResponse(
        val error_code: String,
        val error_message: String
) {

    fun toModel(): ContactCenterError? {
        return if (error_code == "1000") {
            ContactCenterError.ChatSessionBadTenantUrl(error_code)
        } else if (error_code == "2000") {
            ContactCenterError.ChatSessionNoAuthHeader(error_code)
        } else if (error_code == "2001") {
            ContactCenterError.ChatSessionAuthHeaderWrongFormat(error_code)
        } else if (error_code == "2002") {
            ContactCenterError.ChatSessionAuthHeaderBadScheme(error_code)
        } else if (error_code == "2003") {
            ContactCenterError.ChatSessionAuthHeaderMissingAppId(error_code)
        } else if (error_code == "2004") {
            ContactCenterError.ChatSessionAuthHeaderMissingClientId(error_code)
        } else if (error_code == "3000") {
            ContactCenterError.ChatSessionAuthHeaderBadAppId(error_code)
        } else if (error_code == "5000") {
            ContactCenterError.ChatSessionServerTimeout(error_code)
        } else if (error_code == "5001") {
            ContactCenterError.ChatSessionServerNotAvailable(error_code)
        } else if (error_code == "5003") {
            ContactCenterError.ChatSessionInvalidJson(error_code)
        } else if (error_code == "5004") {
            ContactCenterError.ChatSessionServerDisconnected(error_code)
        } else if (error_code == "5005") {
            ContactCenterError.ChatSessionNotFound(error_code)
        } else if (error_code == "5006") {
            ContactCenterError.ChatSessionEntryNotFound(error_code)
        } else if (error_code == "5500") {
            ContactCenterError.ChatSessionInternalServerError(error_code)
        } else if (error_code == "5501") {
            ContactCenterError.ChatSessionUploadSizeLimitExceeded(error_code)
        } else if (error_code == "5502") {
            ContactCenterError.ChatSessionFileNotFound(error_code)
        } else if (error_code == "5509") {
            ContactCenterError.ChatSessionTooManyPollRequests(error_code)
        } else if (error_code == "5511") {
            ContactCenterError.ChatSessionNoEvents(error_code)
        } else if (error_code == "5558") {
            ContactCenterError.ChatSessionFileError(error_code)
        } else if (error_code == "5601") {
            ContactCenterError.ChatSessionCaseNotSpecified(error_code)
        } else if (error_code == "5602") {
            ContactCenterError.ChatSessionCrmServerError(error_code)
        } else if (error_code == "5603") {
            ContactCenterError.ChatSessionTooManyParameters(error_code)
        } else if (error_code == "5955") {
            ContactCenterError.ChatSessionUnspecifiedServerError(error_code)
        } else null
    }
}
