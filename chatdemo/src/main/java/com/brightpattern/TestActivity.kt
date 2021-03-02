package com.brightpattern

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.brightpattern.bpcontactcenter.model.ContactCenterChatSessionProperties
import com.brightpattern.bpcontactcenter.utils.Failure
import com.brightpattern.bpcontactcenter.utils.Success
import com.brightpattern.chatdemo.R
import com.brightpattern.recyclerview.FunctionsListAdapter
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("SetTextI18n")
class TestActivity : AppCompatActivity() {

    val baseURL = "https://alvm.bugfocus.com"
    val tenantURL = "devs.alvm.bugfocus.com"
    val clientID = "D3577669-EB4B-4565-B9C6-27DD857CE8E5"
    val appID = "Android"

    lateinit var api: ContactCenterCommunicator
    lateinit var queue: RequestQueue

    private val adapter = FunctionsListAdapter()
    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.rv)
    }
    private val tvResult: TextView by lazy {
        findViewById(R.id.tvResult)
    }

    private var chatID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)

        queue = Volley.newRequestQueue(applicationContext, object : HurlStack() {
            override fun createConnection(url: URL): HttpURLConnection {
                val connection: HttpURLConnection = super.createConnection(url)
                connection.instanceFollowRedirects = true
                return connection
            }
        })

        api = ContactCenterCommunicator.init(baseURL, tenantURL, appID, clientID, queue)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.selection = { it ->
            when (it) {
                "checkAvailability" -> api.checkAvailability { r -> result(r) }
                "requestChat" -> api.requestChat("555-555-5555", "Someone", JSONObject()) { r -> result(r) }
                "getChatHistory" -> api.getChatHistory(chatID) { r -> result(r) }
                else -> Log.e("EEEEE", "########################################################")
            }
        }

    }

    fun result(result: Any) {
        when (result) {
            is Failure<*> -> {
                Log.e("Failure", ">>> ${result.reason}")
                tvResult.text = "Failure\n${result.reason}"
            }
            is Success<*> -> {
                Log.e("Success", ">>> ${result.value}")
                tvResult.text = "Success\n${result.value}"
                (result.value as? ContactCenterChatSessionProperties)?.let { chatID = it.chatID }
            }
        }
    }
}