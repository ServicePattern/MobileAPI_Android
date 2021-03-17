package com.brightpattern.bpcontactcenter.entity

import org.json.JSONException

sealed class ContactCenterError(text: String) : Error(text) {
    data class FailedToBuildBaseURL(val text: String) : ContactCenterError(text)
    data class FailedToCodeJSON(val event: ContactCenterEvent, val jsonException: JSONException) : ContactCenterError("FailedToCodeJCON")
    data class FailedToCreateURLRequest(val volleyException: com.android.volley.VolleyError) : ContactCenterError("FailedToCreateURLRequest")
    data class BadStatusCode(val statusCode: Int, val volleyException: com.android.volley.VolleyError) : ContactCenterError("badStatusCode")
    data class VolleyError(val volleyException: com.android.volley.VolleyError) : ContactCenterError("VolleyError")
    data class ChatSessionNotFound(val text: String) : ContactCenterError(text)
    data class CommonCCError(val text: String) : ContactCenterError(text)
}