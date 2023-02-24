package com.brightpattern.bpcontactcenter.utils

import com.brightpattern.bpcontactcenter.network.support.HttpHeaderType
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.log10

fun JSONArray.iterator(): Iterator<JSONObject> = (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

fun JSONArray.toList(): List<JSONObject> = (0 until length()).asSequence().map { get(it) as JSONObject }.toList()

fun ByteArray.toBodyEncoded(boundary: String, filename: String): ByteArray {

    var mutableByteArray = byteArrayOf()
    mutableByteArray += "\r\n--$boundary\r\n".encodeToByteArray()
    mutableByteArray += "${HttpHeaderType.ContentDisposition.getPaiDefaultPair().first}: ${HttpHeaderType.ContentDisposition.getPaiDefaultPair().second};".encodeToByteArray()
    mutableByteArray += "name=\"file-upload-input\"; filename=\"${filename}\"\r\n".encodeToByteArray()
    mutableByteArray += "Content-Type: image/jpeg\r\n\r\n".encodeToByteArray()
    mutableByteArray += this
    mutableByteArray += "\r\n--$boundary--\r\n".encodeToByteArray()
    return mutableByteArray
}

val Long.length
    get() = when {
        this == 0L -> 1
        this < 0 -> log10(-toFloat()).toInt() + 2
        else -> log10(toFloat()).toInt() + 1
    }