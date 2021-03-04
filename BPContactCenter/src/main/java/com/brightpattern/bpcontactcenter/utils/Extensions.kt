package com.brightpattern.bpcontactcenter.utils

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.iterator(): Iterator<JSONObject> = (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

fun JSONArray.toList(): List<JSONObject> = (0 until length()).asSequence().map { get(it) as JSONObject }.toList()