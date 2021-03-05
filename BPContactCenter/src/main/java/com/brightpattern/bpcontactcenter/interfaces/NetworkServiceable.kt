package com.brightpattern.bpcontactcenter.interfaces

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.brightpattern.bpcontactcenter.network.support.HttpHeaderFields
import org.json.JSONObject

interface NetworkServiceable {
    val queue: RequestQueue
    var baseURL: String

    fun executeJsonRequest(method: Int, url: String, headerFields: HttpHeaderFields?, jsonRequest: JSONObject?, listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener? = null)
    fun executeSimpleRequest(method: Int, url: String, headerFields: HttpHeaderFields?, listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener? = null)
    fun executePollRequest(method: Int, url: String, headerFields: HttpHeaderFields?, jsonRequest: JSONObject?, timeoutMs: Int, listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener? = null)

}