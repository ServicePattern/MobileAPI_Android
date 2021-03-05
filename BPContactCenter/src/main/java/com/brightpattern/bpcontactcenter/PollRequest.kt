package com.brightpattern.bpcontactcenter

import com.android.volley.Request
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.interfaces.NetworkServiceable
import com.brightpattern.bpcontactcenter.model.http.ContactCenterEventsContainerDto
import com.brightpattern.bpcontactcenter.network.URLProvider
import com.brightpattern.bpcontactcenter.network.support.HttpHeaderFields
import com.brightpattern.bpcontactcenter.utils.Failure
import com.brightpattern.bpcontactcenter.utils.Success
import kotlinx.serialization.json.Json
import kotlin.properties.Delegates

interface PollRequestInterface {
    val pollInterval: Int
    var callback: ContactCenterEventsInterface?
    fun addChatID(chatID: String, baseURL: String, tenantURL: String)
}

class PollRequest private constructor(
        private var defaultHttpHeaderFields: HttpHeaderFields
) : PollRequestInterface {

    companion object {
        fun init(networkService: NetworkServiceable, pollInterval: Int, defaultHttpHeaderFields: HttpHeaderFields): PollRequest {
            return PollRequest(defaultHttpHeaderFields).apply {
                this.pollInterval = pollInterval
                this.networkService = networkService
            }
        }
    }

    override var pollInterval by Delegates.notNull<Int>()
        internal set

    override var callback: ContactCenterEventsInterface? = null
    private var chatID: String = ""
    private lateinit var networkService: NetworkServiceable
    private val format: Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        classDiscriminator = "event"
    }

    // TODO: check for threadsafe !!!!!
    override fun addChatID(chatID: String, baseURL: String, tenantURL: String) {
        this.chatID = chatID
        runObservation(baseURL, tenantURL)
    }

    private fun runObservation(baseURL: String, tenantURL: String) {

        val url = URLProvider.Endpoint.GetNewChatEvents.generateFullUrl(baseURL, tenantURL, chatID)
        if (chatID.isNotEmpty())
            networkService.executePollRequest(Request.Method.GET, url, defaultHttpHeaderFields, null, pollInterval, {
                val result = format.decodeFromString(ContactCenterEventsContainerDto.serializer(), it.toString())
                callback?.chatSessionEvents(Success(result.events))
                runObservation(baseURL, tenantURL)
            }, {
                callback?.chatSessionEvents(Failure(Error(it)))
                if (it.networkResponse.statusCode != 404) {
                    runObservation(baseURL, tenantURL)
                } else {
                    chatID = ""
                }
            })

    }

}