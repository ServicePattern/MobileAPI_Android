package com.brightpattern.bpcontactcenter

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterCommunicating
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.interfaces.NetworkServiceable
import com.brightpattern.bpcontactcenter.model.ContactCenterChatSessionProperties
import com.brightpattern.bpcontactcenter.model.ContactCenterServiceAvailability
import com.brightpattern.bpcontactcenter.model.ContactCenterServiceChatAvailability
import com.brightpattern.bpcontactcenter.network.NetworkService
import com.brightpattern.bpcontactcenter.network.URLProvider
import com.brightpattern.bpcontactcenter.network.support.HttpHeaderFields
import com.brightpattern.bpcontactcenter.network.support.HttpRequestDefaultParameters
import com.brightpattern.bpcontactcenter.utils.Failure
import com.brightpattern.bpcontactcenter.utils.Result
import com.brightpattern.bpcontactcenter.utils.Success
import kotlinx.serialization.json.Json
import org.json.JSONObject

class ContactCenterCommunicator private constructor(override val baseURL: String, override val tenantURL: String, override val appID: String, override val clientID: String) : ContactCenterCommunicating {

    companion object {
        fun init(baseURL: String, tenantURL: String, appID: String, clientID: String, networkService: NetworkServiceable, pollRequestService: PollRequestInterface): ContactCenterCommunicator {
            return ContactCenterCommunicator(baseURL, tenantURL, appID, clientID).apply {
                this.networkService = networkService
                this.networkService.baseURL = baseURL
                this.defaultHttpHeaderFields = HttpHeaderFields.defaultFields(appID, clientID)
                this.defaultHttpRequestParameters = HttpRequestDefaultParameters(tenantURL)
                this.pollRequestService = pollRequestService

            }
        }

        fun init(baseURL: String, tenantURL: String, appID: String, clientID: String, queue: RequestQueue): ContactCenterCommunicator {
            val networkService = NetworkService(queue)
            val pollRequest = PollRequest.init(networkService, 10.0)
            return init(baseURL, tenantURL, appID, clientID, networkService, pollRequest)
        }
    }

    internal lateinit var networkService: NetworkServiceable
    private lateinit var defaultHttpHeaderFields: HttpHeaderFields
    private lateinit var defaultHttpRequestParameters: HttpRequestDefaultParameters
    private lateinit var pollRequestService: PollRequestInterface
    override var delegate: ContactCenterEventsInterface? = null

    private val format = Json { isLenient = true }

    override fun checkAvailability(completion: ((result: Result<ContactCenterServiceAvailability, Error>) -> Unit)) {
        try {
            val url = URLProvider.Endpoint.CheckAvailability.generateFullUrl(baseURL, tenantURL)
            networkService.executeSimpleRequest(Request.Method.GET, url, defaultHttpHeaderFields, {
                val state = format.decodeFromString(ContactCenterServiceAvailability.serializer(), it.toString())
                completion.invoke(Success(state))
            }, {
                completion.invoke(Failure(java.lang.Error(it)))
            })
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(java.lang.Error(e)))
        }
    }

    override fun getChatHistory(chatID: String, completion: (result: Result<List<ContactCenterEvent>, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.GetChatHistory.generateFullUrl(baseURL, tenantURL, chatID)
            networkService.executeSimpleRequest(Request.Method.GET, url, defaultHttpHeaderFields, {
                completion.invoke(Success(listOf()))
            }, {
                completion.invoke(Failure(java.lang.Error(it)))
            })
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(java.lang.Error(e)))
        }
    }

    override fun requestChat(phoneNumber: String, from: String, parameters: JSONObject, completion: (Result<ContactCenterChatSessionProperties, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.RequestChat.generateFullUrl(baseURL, tenantURL)
            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, parameters, {
                val result = format.decodeFromString(ContactCenterChatSessionProperties.serializer(), it.toString())
                completion.invoke(Success(result))
            }, {
                completion.invoke(Failure(java.lang.Error(it)))
            })
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(java.lang.Error(e)))
        }
    }

    override fun sendChatMessage(chatID: String, message: String, completion: (Result<String, Error>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun chatMessageDelivered(chatID: String, messageID: String, completion: (Result<Void, Error>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun chatMessageRead(chatID: String, messageID: String, completion: (Result<Void, Error>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun chatTyping(chatID: String, completion: (Result<Void, Error>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun chatNotTyping(chatID: String, completion: (Result<Void, Error>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun disconnectChat(chatID: String, completion: (Result<Void, Error>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun endChat(chatID: String, completion: (Result<Void, Error>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun subscribeForRemoteNotificationsAPNs(chatID: String, deviceToken: String, completion: (Result<Void, Error>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun subscribeForRemoteNotificationsFirebase(chatID: String, deviceToken: String, completion: (Result<Void, Error>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun appDidReceiveMessage(userInfo: Map<Any, Any>) {
        TODO("Not yet implemented")
    }
}