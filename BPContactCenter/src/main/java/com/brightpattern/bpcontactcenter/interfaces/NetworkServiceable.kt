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
    fun createRequest(method: Int, url: String, headerFields: HttpHeaderFields, parameters: String?, body: String?): JsonObjectRequest
    fun executeSimpleRequest(method: Int, url: String, headerFields: HttpHeaderFields?, listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener? = null)

    /*
    /// Create a request given requestMethod  (get, post, create, etc...),  a URL,  and header fields
    func createRequest(method: HttpMethod, url: URL, headerFields: HttpHeaderFields?, body: Encodable?) throws -> URLRequest
    /// Create a request given requestMethod  (get, post, create, etc...),  a base URL, endpoint and header fields
    /// To create  a request with special header files that represent authorization, content type and user agent use [HttpHeaderFields](x-source-tag://HttpHeaderFields)
    /// That header fields are usually sent inside the requests to the backend
    /// Exception might be done for requests that load data from AWS for ex.
    ///  Parameters baseURL and endpoint are used to build a complete URL
    /// - Tag: createRequest
    func createRequest(method: HttpMethod, baseURL: URL, endpoint: URLProvider.Endpoint, headerFields: HttpHeaderFields, parameters: Encodable?, body: Encodable?) throws -> URLRequest?
    func decode<T: Decodable>(to type: T.Type, data: Data) throws -> T
    @discardableResult
    func dataTask(using request: URLRequest, with completion: @escaping (NetworkDataResponse) -> Void) -> URLSessionDataTask
    @discardableResult
    func dataTask<T: Decodable>(using request: URLRequest, with completion: @escaping (Result<T, Error>) -> Void) -> URLSessionDataTask
    @discardableResult
    func dataTask(using request: URLRequest, with completion: @escaping (NetworkVoidResponse) -> Void) -> URLSessionDataTask
     */
}