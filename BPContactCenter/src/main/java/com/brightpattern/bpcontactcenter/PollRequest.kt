package com.brightpattern.bpcontactcenter

import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.interfaces.NetworkServiceable

interface PollRequestInterface{
    var callback: ContactCenterEventsInterface?
    fun addChatID(chatID: String)
}

class PollRequest private constructor(internal val pollInterval: Double) : PollRequestInterface {

    companion object{
        fun init(networkService: NetworkServiceable, pollInterval: Double): PollRequest{
            return PollRequest(pollInterval).apply {
                this.networkService = networkService
            }
        }
    }

    override var callback: ContactCenterEventsInterface? = null

    override fun addChatID(chatID: String) {
        TODO("Not yet implemented")
    }

    private lateinit var networkService: NetworkServiceable

}