package com.brightpattern.bpcontactcenter.entity

import org.json.JSONException

sealed class ContactCenterError(text: String) : Error(text) {
    data class FailedToBuildBaseURL(val text: String) : ContactCenterError(text)
    data class FailedTenantURL(val text: String) : ContactCenterError(text)
    data class FailedToCodeJSON(val event: ContactCenterEvent, val jsonException: JSONException) : ContactCenterError("FailedToCodeJCON")
    data class FailedToCreateURLRequest(val volleyException: com.android.volley.VolleyError) : ContactCenterError("FailedToCreateURLRequest")
    data class BadStatusCode(val statusCode: Int, val volleyException: com.android.volley.VolleyError) : ContactCenterError("badStatusCode")
    data class VolleyError(val volleyException: com.android.volley.VolleyError) : ContactCenterError("VolleyError")

    data class ChatSessionBadTenantUrl(val text: String) : ContactCenterError(text)
    data class ChatSessionNoAuthHeader(val text: String) : ContactCenterError(text)
    data class ChatSessionAuthHeaderWrongFormat(val text: String) : ContactCenterError(text)
    data class ChatSessionAuthHeaderBadScheme(val text: String) : ContactCenterError(text)
    data class ChatSessionAuthHeaderMissingAppId(val text: String) : ContactCenterError(text)
    data class ChatSessionAuthHeaderMissingClientId(val text: String) : ContactCenterError(text)
    data class ChatSessionAuthHeaderBadAppId(val text: String) : ContactCenterError(text)
    data class ChatSessionServerTimeout(val text: String) : ContactCenterError(text)
    data class ChatSessionServerNotAvailable(val text: String) : ContactCenterError(text)
    data class ChatSessionInvalidJson(val text: String) : ContactCenterError(text)
    data class ChatSessionServerDisconnected(val text: String) : ContactCenterError(text)
    data class ChatSessionNotFound(val text: String) : ContactCenterError(text)
    data class ChatSessionEntryNotFound(val text: String) : ContactCenterError(text)
    data class ChatSessionInternalServerError(val text: String) : ContactCenterError(text)
    data class ChatSessionUploadSizeLimitExceeded(val text: String) : ContactCenterError(text)
    data class ChatSessionFileNotFound(val text: String) : ContactCenterError(text)
    data class ChatSessionTooManyPollRequests(val text: String) : ContactCenterError(text)
    data class ChatSessionNoEvents(val text: String) : ContactCenterError(text)
    data class ChatSessionFileError(val text: String) : ContactCenterError(text)
    data class ChatSessionCaseNotSpecified(val text: String) : ContactCenterError(text)
    data class ChatSessionCrmServerError(val text: String) : ContactCenterError(text)
    data class ChatSessionTooManyParameters(val text: String) : ContactCenterError(text)
    data class ChatSessionUnspecifiedServerError(val text: String) : ContactCenterError(text)

    data class CommonCCError(val text: String) : ContactCenterError(text)
}