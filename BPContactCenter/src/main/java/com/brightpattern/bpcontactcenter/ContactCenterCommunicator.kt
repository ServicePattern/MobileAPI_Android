package com.brightpattern.bpcontactcenter

import android.content.Context
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import com.brightpattern.bpcontactcenter.entity.ContactCenterError
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.entity.FieldName
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterCommunicating
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.interfaces.NetworkServiceable
import com.brightpattern.bpcontactcenter.model.ContactCenterChatSessionProperties
import com.brightpattern.bpcontactcenter.model.ContactCenterServiceAvailability
import com.brightpattern.bpcontactcenter.model.http.ChatSessionCaseHistoryDto
import com.brightpattern.bpcontactcenter.model.http.ContactCenterEventsContainerDto
import com.brightpattern.bpcontactcenter.network.NetworkService
import com.brightpattern.bpcontactcenter.network.URLProvider
import com.brightpattern.bpcontactcenter.network.support.HttpHeaderFields
import com.brightpattern.bpcontactcenter.network.support.HttpRequestDefaultParameters
import com.brightpattern.bpcontactcenter.utils.Failure
import com.brightpattern.bpcontactcenter.utils.Result
import com.brightpattern.bpcontactcenter.utils.Success
import kotlinx.serialization.json.Json
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 *  ContactCenterCommunicator is the entry point to use API
 *  create an instance of class using static init functions
 */
class ContactCenterCommunicator private constructor(override val baseURL: String, override val tenantURL: String, override val appID: String, override val clientID: String) : ContactCenterCommunicating {

    companion object {
        /**
         *  This static function returns an instance of ContactCenterCommunicator class
         *  @param baseURL your server URL
         *  @param tenantURL your tenant URL
         *  @param appID your messaging scenario entry ID
         *  @param clientID unique `clientID` for your application
         *  @param networkService [NetworkServiceable]
         *  @param pollRequestService [PollRequestInterface]
         *
         *  @return [ContactCenterCommunicator]
         */
        fun init(baseURL: String, tenantURL: String, appID: String, clientID: String, networkService: NetworkServiceable, pollRequestService: PollRequestInterface): ContactCenterCommunicator {
            return ContactCenterCommunicator(baseURL, tenantURL, appID, clientID).apply {
                this.networkService = networkService
                this.networkService.baseURL = baseURL
                this.defaultHttpHeaderFields = HttpHeaderFields.defaultFields(appID, clientID)
                this.defaultHttpRequestParameters = HttpRequestDefaultParameters(tenantURL)
                this.pollRequestService = pollRequestService

            }
        }

        /**
         *  This simple static function returns an instance of ContactCenterCommunicator class
         *  @param baseURL your server URL
         *  @param tenantURL your tenant URL
         *  @param appID your messaging scenario entry ID
         *  @param clientID unique `clientID` for your application
         *  @param context application Context
         *
         *  @return [ContactCenterCommunicator]
         */
        fun init(baseURL: String, tenantURL: String, appID: String, clientID: String, context: Context): ContactCenterCommunicator {
            val queue = Volley.newRequestQueue(context, object : HurlStack() {
                override fun createConnection(url: URL): HttpURLConnection {
                    val connection: HttpURLConnection = super.createConnection(url)
                    connection.instanceFollowRedirects = true
                    return connection
                }
            })
            val networkService = NetworkService(queue)
            val pollRequest = PollRequest.init(networkService, 20000, HttpHeaderFields.defaultFields(appID, clientID))
            return init(baseURL, tenantURL, appID, clientID, networkService, pollRequest).apply {
                checkAvailability {
                    callback?.chatSessionEvents(Failure(java.lang.Error("checkAvailability error")))
                }
            }
        }
    }

    internal lateinit var networkService: NetworkServiceable
    private lateinit var defaultHttpHeaderFields: HttpHeaderFields
    private lateinit var defaultHttpRequestParameters: HttpRequestDefaultParameters
    private lateinit var pollRequestService: PollRequestInterface
    override var delegate: ContactCenterEventsInterface? = null

    private val format = Json {
        isLenient = true
        ignoreUnknownKeys = true
        classDiscriminator = FieldName.EVENT
    }

    /**
     * time between callback requests
     */
    var callbackWaitingTimeMS: Int
        get() {
            return pollRequestService.pollInterval
        }
        set(value) {
            (pollRequestService as PollRequest).pollInterval = value
        }

    var callback: ContactCenterEventsInterface?
        get() {
            return pollRequestService.callback
        }
        set(value) {
            pollRequestService.callback = value
        }

    override fun checkAvailability(completion: ((result: Result<ContactCenterServiceAvailability, Error>) -> Unit)) {
        try {
            val url = URLProvider.Endpoint.CheckAvailability.generateFullUrl(baseURL, tenantURL)
            networkService.executeSimpleRequest(Request.Method.GET, url, defaultHttpHeaderFields, {
                val state = format.decodeFromString(ContactCenterServiceAvailability.serializer(), it.toString())
                completion.invoke(Success(state))
            }, {
                it.localizedMessage?.let{ localizedMessage ->
                    completion.invoke(Failure(ContactCenterError.CommonCCError(localizedMessage)))
                } ?: run{
                    completion.invoke(Failure(ContactCenterError.CommonCCError("No localized message, please provide the info to the dev team")))
                }
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun getChatHistory(chatID: String, completion: (result: Result<List<ContactCenterEvent>, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.GetChatHistory.generateFullUrl(baseURL, tenantURL, chatID)
            networkService.executeSimpleRequest(Request.Method.GET, url, defaultHttpHeaderFields, {
                val result = format.decodeFromString(ContactCenterEventsContainerDto.serializer(), it.toString())
                completion.invoke(Success(result.events))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun getCaseHistory(chatID: String, completion: (result: Result<ChatSessionCaseHistoryDto, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.GetCaseHistory.generateFullUrl(baseURL, tenantURL, chatID)
            networkService.executeSimpleRequest(Request.Method.GET, url, defaultHttpHeaderFields, {
                val list = format.decodeFromString(ChatSessionCaseHistoryDto.serializer(), it.toString())
                completion.invoke(Success(list))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun requestChat(phoneNumber: String, from: String, parameters: JSONObject, completion: (Result<ContactCenterChatSessionProperties, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.RequestChat.generateFullUrl(baseURL, tenantURL)
            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, parameters, {
                val result = format.decodeFromString(ContactCenterChatSessionProperties.serializer(), it.toString())
                pollRequestService.addChatID(result.chatID, baseURL, tenantURL)
                completion.invoke(Success(result))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun sendChatMessage(chatID: String, message: String, messageID: UUID?, completion: (Result<String, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.SendEvents.generateFullUrl(baseURL, tenantURL, chatID)
            val payload = createSendEventPayload(ContactCenterEvent.ChatSessionMessage(messageID.toString(), UUID.randomUUID().toString(), message))

            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, payload, {
                completion.invoke(Success(it.toString()))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun chatMessageDelivered(chatID: String, messageID: String, completion: (Result<String, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.SendEvents.generateFullUrl(baseURL, tenantURL, chatID)
            val payload = createSendEventPayload(ContactCenterEvent.ChatSessionMessageDelivered(messageID, null))

            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, payload, {
                completion.invoke(Success(it.toString()))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun chatMessageRead(chatID: String, messageID: String, completion: (Result<String, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.SendEvents.generateFullUrl(baseURL, tenantURL, chatID)
            val payload = createSendEventPayload(ContactCenterEvent.ChatSessionMessageRead(messageID, null))

            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, payload, {
                completion.invoke(Success(it.toString()))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun chatTyping(chatID: String, completion: (Result<String, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.SendEvents.generateFullUrl(baseURL, tenantURL, chatID)
            val payload = createSendEventPayload(ContactCenterEvent.ChatSessionTyping(null))

            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, payload, {
                completion.invoke(Success(it.toString()))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun chatNotTyping(chatID: String, completion: (Result<String, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.SendEvents.generateFullUrl(baseURL, tenantURL, chatID)
            val payload = createSendEventPayload(ContactCenterEvent.ChatSessionNotTyping(null))

            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, payload, {
                completion.invoke(Success(it.toString()))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun disconnectChat(chatID: String, completion: (Result<String, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.SendEvents.generateFullUrl(baseURL, tenantURL, chatID)
            val payload = createSendEventPayload(ContactCenterEvent.ChatSessionDisconnect())

            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, payload, {
                completion.invoke(Success(it.toString()))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun endChat(chatID: String, completion: (Result<String, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.SendEvents.generateFullUrl(baseURL, tenantURL, chatID)
            val payload = createSendEventPayload(ContactCenterEvent.ChatSessionEnd())

            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, payload, {
                completion.invoke(Success(it.toString()))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun subscribeForRemoteNotificationsAPNs(chatID: String, deviceToken: String, completion: (Result<Void, Error>) -> Unit) {
        completion(Failure(java.lang.Error("Android has NO APN")))
    }

    override fun subscribeForRemoteNotificationsFirebase(chatID: String, deviceToken: String, completion: (Result<String, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.SubscribeForNotifications.generateFullUrl(baseURL, tenantURL, chatID)
            val json = JSONObject()
            json.put(FieldName.DEVICE_TOKEN, deviceToken)
            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, json, {
                completion.invoke(Success(it.toString()))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    override fun appDidReceiveMessage(userInfo: Map<Any, Any>) {
        userInfo[FieldName.NOTIFICATION_CHAT_ID]?.let {
            (it as String)
            pollRequestService.addChatID(it, baseURL, tenantURL)
        }
    }

    override fun closeCase(chatID: String, completion: (Result<String, Error>) -> Unit) {
        try {
            val url = URLProvider.Endpoint.CloseCase.generateFullUrl(baseURL, tenantURL, chatID)
            val payload = createSendEventPayload(ContactCenterEvent.ChatSessionEnd())

            networkService.executeJsonRequest(Request.Method.POST, url, defaultHttpHeaderFields, payload, {
                completion.invoke(Success(it.toString()))
            }, {
                completion.invoke(Failure(parseVolleyError(it)))
            })
        } catch (e: ContactCenterError) {
            completion.invoke(Failure(e))
        } catch (e: java.lang.Exception) {
            completion.invoke(Failure(ContactCenterError.CommonCCError(e.toString())))
        }
    }

    @Throws(ContactCenterError::class)
    private fun createSendEventPayload(event: ContactCenterEvent): JSONObject {
        val container = ContactCenterEventsContainerDto(listOf(event))
        try {
            return JSONObject(format.encodeToString(ContactCenterEventsContainerDto.serializer(), container))
        } catch (e: JSONException) {
            throw ContactCenterError.FailedToCodeJSON(event, e)
        }
    }

    private fun parseVolleyError(volleyError: VolleyError): ContactCenterError {
        return when  {
            volleyError.networkResponse.statusCode !in 200..299 -> ContactCenterError.BadStatusCode(volleyError.networkResponse.statusCode, volleyError)
            volleyError is TimeoutError || volleyError is NoConnectionError -> ContactCenterError.FailedToCreateURLRequest(volleyError)
            else -> ContactCenterError.VolleyError(volleyError)
        }

    }
}