package com.brightpattern.bpcontactcenter.network.support

/// Used when the endpoint requires a header-type (i.e. "content-type") be specified in the header
enum class HttpHeaderType(val value: String) {
    ContentType("Content-Type"),
    ImageContentType("Content-Type"),
    Authorization("Authorization"),
    UserAgent("UserAgent"),
    CacheControl("Cache-Control"),
    ContentDisposition("Content-Disposition");

    fun getPaiDefaultPair(appID: String = "", clientID: String = "", boundary: String = ""): Pair<String, String> {
        return when (this) {
            ContentType -> value to "application/json; charset=utf-8"
            ImageContentType -> value to "multipart/form-data; boundary=$boundary"
            Authorization -> value to "MOBILE-API-140-327-PLAIN appId=$appID, clientId=$clientID"
            UserAgent -> value to "MobileClient"
            CacheControl -> value to "no-cache"
            ContentDisposition -> value to "form-data"
        }
    }
}