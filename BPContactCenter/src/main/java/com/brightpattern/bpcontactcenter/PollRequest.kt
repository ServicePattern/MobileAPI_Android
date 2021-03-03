package com.brightpattern.bpcontactcenter

import android.util.Log
import com.android.volley.Request
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.interfaces.NetworkServiceable
import com.brightpattern.bpcontactcenter.network.URLProvider
import com.brightpattern.bpcontactcenter.network.support.HttpHeaderFields
import com.brightpattern.bpcontactcenter.utils.Failure
import com.brightpattern.bpcontactcenter.utils.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

interface PollRequestInterface {
    val pollInterval: Long
    var callback: ContactCenterEventsInterface?
    fun addChatID(chatID: String)
    fun runObservation(baseURL: String, tenantURL: String)
}

class PollRequest private constructor( 
                                      private var defaultHttpHeaderFields: HttpHeaderFields
) : PollRequestInterface {

    companion object {
        fun init(networkService: NetworkServiceable, pollInterval: Long, defaultHttpHeaderFields: HttpHeaderFields): PollRequest {
            return PollRequest(defaultHttpHeaderFields).apply {
                this.pollInterval = pollInterval
                this.networkService = networkService
            }
        }
    }

    override var pollInterval by Delegates.notNull<Long>()
        internal set

    override var callback: ContactCenterEventsInterface? = null
    private var chatID: String = ""
    private lateinit var networkService: NetworkServiceable

    // TODO: Threadsafe !!!!!
    override fun addChatID(chatID: String) {
        this.chatID = chatID
    }

    override fun runObservation(baseURL: String, tenantURL: String) {


        GlobalScope.launch {
            val dispatcher = this.coroutineContext
            CoroutineScope(dispatcher).launch {
                while (true) {
                    val url = URLProvider.Endpoint.GetNewChatEvents.generateFullUrl(baseURL, tenantURL, chatID)
                    if (chatID.isNotEmpty())
                        networkService.executeJsonRequest(Request.Method.GET, url, defaultHttpHeaderFields, null, {
                            val result = ContactCenterEvent.listFromJSONEvents(it)
                            callback?.chatSessionEvents(Success(result))
                        }, {
                            callback?.chatSessionEvents(Failure(Error(it)))
                        })
                    delay(pollInterval)
                }
            }
        }
    }

}