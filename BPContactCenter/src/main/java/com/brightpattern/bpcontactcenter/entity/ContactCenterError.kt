package com.brightpattern.bpcontactcenter.entity

import com.brightpattern.bpcontactcenter.network.ContactCenterErrorResponse
import java.lang.Error

sealed class ContactCenterError: Error() {
    class failedToBuildBaseURL: ContactCenterError()
    class failedToCodeJCON(val nestedErrors: List<Error>): ContactCenterError()
    class failedToCreateURLRequest(): ContactCenterError()
    class badStatusCode(statusCode: Int, errorResponse: ContactCenterErrorResponse?): ContactCenterError()
    class unexpectedResponse(val response: String?): ContactCenterError()
    class dataEmpty: ContactCenterError()
    class failedToCast(val value: String): ContactCenterError()
    class chatSessionNotFound: ContactCenterError()
}