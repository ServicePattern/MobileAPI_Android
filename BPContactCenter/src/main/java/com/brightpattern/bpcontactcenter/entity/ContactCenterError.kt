package com.brightpattern.bpcontactcenter.entity

import com.brightpattern.bpcontactcenter.network.ContactCenterErrorResponse

sealed class ContactCenterError(text: String): Error(text) {
    data class FailedToBuildBaseURL(val text: String): ContactCenterError(text)
    data class FailedToCodeJCON(val nestedErrors: List<Error>): ContactCenterError("failedToCodeJCON")
    data class FailedToCreateURLRequest(val text: String): ContactCenterError(text)
    data class BadStatusCode(val statusCode: Int, val errorResponse: ContactCenterErrorResponse?): ContactCenterError("badStatusCode")
    data class UnexpectedResponse(val text: String, val response: String?): ContactCenterError(text)
    data class DataEmpty(val text: String): ContactCenterError(text)
    data class FailedToCast(val value: String): ContactCenterError(value)
    data class ChatSessionNotFound(val text: String): ContactCenterError(text)
    data class CommonCCError(val text: String): ContactCenterError(text)
}