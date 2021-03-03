package com.brightpattern

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.model.ContactCenterChatSessionProperties
import com.brightpattern.bpcontactcenter.utils.Failure
import com.brightpattern.bpcontactcenter.utils.Result
import com.brightpattern.bpcontactcenter.utils.Success
import com.brightpattern.chatdemo.R
import com.brightpattern.recyclerview.FunctionsListAdapter
import org.json.JSONObject
import java.util.*

@SuppressLint("SetTextI18n")
class TestActivity : AppCompatActivity() {

    val baseURL = "https://alvm.bugfocus.com"
    val tenantURL = "devs.alvm.bugfocus.com"
    val clientID = UUID.randomUUID().toString()
    val appID = "Android"

    lateinit var api: ContactCenterCommunicator

    private val adapter = FunctionsListAdapter()
    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.rv)
    }
    private val tvResult: TextView by lazy {
        findViewById(R.id.tvResult)
    }

    private var chatID: String = ""
    private var partyID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)

        api = ContactCenterCommunicator.init(baseURL, tenantURL, appID, clientID, applicationContext)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.selection = { it ->
            when (it) {
                "checkAvailability" -> api.checkAvailability { r -> resultProcessing(r) }
                "requestChat" -> api.requestChat("555-555-5555", "Someone", JSONObject()) { r -> resultProcessing(r) }
                "getChatHistory" -> api.getChatHistory(chatID) { r -> resultProcessing(r) }
                "getCaseHistory" -> api.getCaseHistory(chatID) { r -> resultProcessing(r) }
                "sendChatMessage" -> api.sendChatMessage(chatID, "MY MESSAGE") { r -> resultProcessing(r) }
                else -> Log.e("EEEEE", "########################################################")
            }
        }

        api.callback = object: ContactCenterEventsInterface {
            override fun chatSessionEvents(result: Result<List<ContactCenterEvent>, Error>) {
                Log.e("&&&&&&&&&&&&", " &&&&&&&&&&&&&&&&&&&&&&&&&&& \t\n\t $result")
                this@TestActivity.resultProcessing(result)
            }
        }

    }

    fun resultProcessing(result: Any) {
        when (result) {
            is Failure<*> -> {
                Log.e("Failure", ">>> ${result.reason}")
                tvResult.text = "Failure\n${result.reason}"
            }
            is Success<*> -> {
                Log.e("Success", ">>> ${result.value}")
                tvResult.text = "Success\n${result.value}"
                (result.value as? ContactCenterChatSessionProperties)?.let {
                    chatID = it.chatID
                    partyID = it.chatID
                }
            }
        }
    }
}