package com.brightpattern.bpcontactcenter.network.support


/// - Tag: HttpHeaderFields
data class HttpHeaderFields (val fields: Map<String, String>) {

    companion object {
        /// Create an instance with authorization, content type and user agent use that are usually sent with each request
        fun defaultFields(appID: String, clientID: String): HttpHeaderFields {
            val headerMap = mapOf(
                    HttpHeaderType.Authorization.getPaiDefaultPair(appID, clientID),
                    HttpHeaderType.ContentType.getPaiDefaultPair(),
                    HttpHeaderType.UserAgent.getPaiDefaultPair())
            return HttpHeaderFields(headerMap)
        }
    }
}
