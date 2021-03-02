package com.brightpattern.bpcontactcenter.network

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.brightpattern.bpcontactcenter.interfaces.NetworkServiceable
import com.brightpattern.bpcontactcenter.network.support.HttpHeaderFields
import com.brightpattern.bpcontactcenter.network.support.HttpRequestDefaultParameters
import org.json.JSONObject

class NetworkService(override val queue: RequestQueue) : NetworkServiceable {

    override lateinit var baseURL: String

    override fun executeJsonRequest(method: Int, url: String, headerFields: HttpHeaderFields?, jsonRequest: JSONObject?, listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener?) {
        val request = object : JsonObjectRequest(
                method,
                url,
                jsonRequest,
                listener,
                errorListener) {
            override fun getHeaders(): MutableMap<String, String> {
                return headerFields?.fields?.toMutableMap() ?: mutableMapOf()
            }
        }
        queue.add(request)
    }

    override fun executeSimpleRequest(method: Int, url: String, headerFields: HttpHeaderFields?, listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener?) {
        val request = object : JsonObjectRequest(
                method,
                url,
                null,
                listener,
                errorListener) {
            override fun getHeaders(): MutableMap<String, String> {
                return headerFields?.fields?.toMutableMap() ?: mutableMapOf()
            }
        }
        queue.add(request)
    }

    override fun createRequest(method: Int, url: String, headerFields: HttpHeaderFields, parameters: String?, body: String?): JsonObjectRequest {
        TODO("Not yet implemented")
    }
}