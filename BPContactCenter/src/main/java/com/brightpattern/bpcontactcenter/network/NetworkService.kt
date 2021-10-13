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

                response?.let { logResponse(it) }
                return super.parseNetworkResponse(response)
            }
        }

        logRequest(request)

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
                response?.let { logResponse(it) }
                return super.parseNetworkResponse(response)
            }
        }

        logRequest(request)

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

                response?.let { logResponse(it) }

                return super.parseNetworkResponse(response)
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
                timeoutMs,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        logRequest(request)

        queue.add(request)
    }

    private fun logRequest(request: JsonObjectRequest) {
        if (!BuildConfig.DEBUG)
            return

        var logStr = "\n---------- OUT ---------->\n"

        when (request.method) {
            Request.Method.GET -> logStr += "GET"
            Request.Method.DELETE -> logStr += "DELETE"
            Request.Method.HEAD -> logStr += "HEAD"
            Request.Method.OPTIONS -> logStr += "OPTIONS"
            Request.Method.PATCH -> logStr += "PATCH"
            Request.Method.POST -> logStr += "POST"
            Request.Method.PUT -> logStr += "PUT"
            Request.Method.TRACE -> logStr += "TRACE"
        }

        logStr += " ${request.url} HTTP/1.1\n"
//        requestLog += "Host: \(host)\n"
        request.headers.forEach {
            logStr += "${it.key}: ${it.value}\n"
        }

        logStr += "\n${request.body?.let { String(it) }}\n"

        logStr += "\n------------------------->\n";

        Log.d("NetworkService", logStr)
    }

    private fun logResponse(response: NetworkResponse) {
        if (!BuildConfig.DEBUG)
            return

        var logStr = "\n---------- IN ---------->\n"

        logStr += "${response.statusCode}\n"
        response.headers?.forEach {
            logStr += "${it.key}: ${it.value}\n"
        }

        logStr += "\n${response.data?.let { String(it) }}\n"

        logStr += "\n------------------------->\n";

        Log.d("NetworkService", logStr)
    }
}
