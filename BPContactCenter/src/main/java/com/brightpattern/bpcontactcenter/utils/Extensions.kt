package com.brightpattern.bpcontactcenter.utils

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.log10

fun JSONArray.iterator(): Iterator<JSONObject> = (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

fun JSONArray.toList(): List<JSONObject> = (0 until length()).asSequence().map { get(it) as JSONObject }.toList()

val Long.length
    get() = when {
        this == 0L -> 1
        this < 0 -> log10(-toFloat()).toInt() + 2
        else -> log10(toFloat()).toInt() + 1
    }