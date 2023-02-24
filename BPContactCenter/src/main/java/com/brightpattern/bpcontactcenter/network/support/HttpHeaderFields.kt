package com.brightpattern.bpcontactcenter.network.support


/// - Tag: HttpHeaderFields
data class HttpHeaderFields(val fields: Map<String, String>) {

    companion object {
        /// Create an instance with authorization, content type and user agent use that are usually sent with each request
        fun defaultFields(appID: String, clientID: String): HttpHeaderFields {
            val headerMap = mapOf(
                    HttpHeaderType.Authorization.getPaiDefaultPair(appID, clientID),
                    HttpHeaderType.ContentType.getPaiDefaultPair(),
                    HttpHeaderType.UserAgent.getPaiDefaultPair(),
                    HttpHeaderType.CacheControl.getPaiDefaultPair())
            return HttpHeaderFields(headerMap)
        }
    }

    fun fileUploadFields(boundary: String): HttpHeaderFields {
        val imageContentTypePair = HttpHeaderType.ImageContentType.getPaiDefaultPair(boundary = boundary)
        val newFieldsMap = fields.filter { it.key != HttpHeaderType.ContentType.value }.toMutableMap()
        newFieldsMap[imageContentTypePair.first] = imageContentTypePair.second

        return HttpHeaderFields(newFieldsMap)
    }
}
