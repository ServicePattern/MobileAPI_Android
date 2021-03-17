package com.brightpattern.bpcontactcenter.network

import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.brightpattern.bpcontactcenter.BuildConfig
import com.brightpattern.bpcontactcenter.entity.FieldName
import com.brightpattern.bpcontactcenter.interfaces.NetworkServiceable
import com.brightpattern.bpcontactcenter.network.support.HttpHeaderFields
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

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                if (response?.statusCode == 200 && response.data.isEmpty()) {
                    val responseObject = JSONObject()
                    responseObject.put(FieldName.STATE, "success")
                    return Response.success(responseObject, HttpHeaderParser.parseCacheHeaders(response))
                }
                if (BuildConfig.DEBUG)
                    Log.d("NetworkService", "Received from server: ${response?.data}")
                return super.parseNetworkResponse(response)
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

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                if (BuildConfig.DEBUG)
                    Log.d("NetworkService", "Received from server: ${response?.data}")
                return super.parseNetworkResponse(response)
            }
        }
        queue.add(request)
    }

    override fun executePollRequest(method: Int, url: String, headerFields: HttpHeaderFields?, jsonRequest: JSONObject?, timeoutMs: Int, listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener?) {
        val request = object : JsonObjectRequest(
                method,
                url,
                jsonRequest,
                listener,
                errorListener) {
            override fun getHeaders(): MutableMap<String, String> {
                return headerFields?.fields?.toMutableMap() ?: mutableMapOf()
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                if (response?.statusCode == 200 && response.data.isEmpty()) {
                    val responseObject = JSONObject()
                    responseObject.put(FieldName.STATE, "success")
                    return Response.success(responseObject, HttpHeaderParser.parseCacheHeaders(response))
                }
                if (BuildConfig.DEBUG)
                    Log.d("NetworkService", "Received from server: ${response?.data}")
                return super.parseNetworkResponse(response)
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
                timeoutMs,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(request)
    }
}
