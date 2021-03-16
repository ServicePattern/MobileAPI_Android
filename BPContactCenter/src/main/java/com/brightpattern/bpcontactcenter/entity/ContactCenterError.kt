package com.brightpattern.bpcontactcenter.entity

import com.brightpattern.bpcontactcenter.network.ContactCenterErrorResponse

sealed class ContactCenterError(text: String): Error(text) {
    data class failedToBuildBaseURL(val text: String): ContactCenterError(text)
    data class failedToCodeJCON(val nestedErrors: List<Error>): ContactCenterError("failedToCodeJCON")
    data class failedToCreateURLRequest(val text: String): ContactCenterError(text)
    data class badStatusCode(val statusCode: Int, val errorResponse: ContactCenterErrorResponse?): ContactCenterError("badStatusCode")
    data class unexpectedResponse(val text: String, val response: String?): ContactCenterError(text)
    data class dataEmpty(val text: String): ContactCenterError(text)
    data class failedToCast(val value: String): ContactCenterError(value)
    data class chatSessionNotFound(val text: String): ContactCenterError(text)
}