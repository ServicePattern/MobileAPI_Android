package com.brightpattern.bpcontactcenter

import com.android.volley.Request
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
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
    fun startPolling(chatID: String) : Boolean
    fun stopPolling(chatID: String) : Boolean
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
    private var baseUrl: String = ""
    private var tenantUrl: String = ""
    private var isPaused: Boolean = true
    private lateinit var networkService: NetworkServiceable
    private val format: Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        classDiscriminator = "event"
    }

    // TODO: check for threadsafe !!!!!
    override fun addChatID(chatID: String, baseURL: String, tenantURL: String) {
        this.chatID = chatID
        this.baseUrl = baseURL
        this.tenantUrl = tenantURL
        this.isPaused = false
        runObservation()
    }

    override fun startPolling(chatID: String) : Boolean {
        if (this.chatID.isEmpty() || !chatID.equals(this.chatID) || !isPaused) {
            return false
        }

        isPaused = false;
        runObservation()

        return true;
    }

    override fun stopPolling(chatID: String) : Boolean {
        if (this.chatID.isEmpty() || !chatID.equals(this.chatID)) {
            return false
        }

        isPaused = true

        return true
    }

    private fun runObservation() {

        val url = URLProvider.Endpoint.GetNewChatEvents.generateFullUrl(baseUrl, tenantUrl, chatID)
        if (chatID.isNotEmpty() && !isPaused)
            networkService.executePollRequest(Request.Method.GET, url, defaultHttpHeaderFields, null, pollInterval, {
                val result = format.decodeFromString(ContactCenterEventsContainerDto.serializer(), it.toString())
                //  Add URL for file events
                result.events?.forEach {
                    (it as? ContactCenterEvent.ChatSessionFile)?.let { message ->
                        message.url = URLProvider.Endpoint.File.generateFileUrl(baseUrl, message.fileUUID)
                    }
                }

                callback?.chatSessionEvents(Success(result.events))
                runObservation()
            }, {
                callback?.chatSessionEvents(Failure(Error(it)))
                if (it.networkResponse.statusCode != 404) {
                    runObservation()
                } else {
                    chatID = ""
                }
            })

    }

}