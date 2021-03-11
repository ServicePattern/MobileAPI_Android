package com.brightpattern

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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

    val api: ContactCenterCommunicator?
        get() {
            return ChatDemo.api
        }

    private val adapter = FunctionsListAdapter()
    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.rv)
    }
    private val tvResult: TextView by lazy {
        findViewById(R.id.tvResult)
    }

    private val btnRunMessaging: Button by lazy {
        findViewById(R.id.btnRunMessaging)
    }

    private val btnPreferences: Button by lazy {
        findViewById(R.id.btnRunPref)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)

        btnRunMessaging.setOnClickListener {
            startActivityForResult(Intent(applicationContext, MessageActivity::class.java), ChatDemo.OPEN_MESSAGE_ACTIVITY_REQUEST_ID)
        }
        btnPreferences.setOnClickListener {
            startActivity(Intent(applicationContext, PreferencesActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ChatDemo.OPEN_MESSAGE_ACTIVITY_REQUEST_ID && resultCode == ChatDemo.CLOSED_BY_SERVER) {
            ChatDemo.chatID = ""
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        Log.e("TestActivity", "************** onResume **************")
        super.onResume()
        initAPI()
        btnRunMessaging.isEnabled = ChatDemo.chatID.isNotEmpty()
    }

    private fun initAPI() {
        api?.let { api ->
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter
            adapter.selection = { it ->
                Log.e("FB", "${ChatDemo.gcmToken}")
                when (it) {
                    "checkAvailability" -> api.checkAvailability { r -> resultProcessing(r) }
                    "requestChat" -> api.requestChat("555-555-5555", "Someone", JSONObject()) { r -> resultProcessing(r) }
                    "getChatHistory" -> api.getChatHistory(ChatDemo.chatID) { r -> resultProcessing(r) }
                    "getCaseHistory" -> api.getCaseHistory(ChatDemo.chatID) { r -> resultProcessing(r) }
                    "sendChatMessage" -> api.sendChatMessage(ChatDemo.chatID, "MY MESSAGE") { r -> resultProcessing(r) }
                    "subscribeForRemoteNotificationsFirebase" -> api.subscribeForRemoteNotificationsFirebase(ChatDemo.chatID, ChatDemo.gcmToken ?: "unknown") { r -> resultProcessing(r) }
                    "subscribeForRemoteNotificationsAPNs" -> api.subscribeForRemoteNotificationsAPNs(ChatDemo.chatID, ChatDemo.gcmToken ?: "unknown") { r -> resultProcessing(r) }
                    "chatMessageDelivered" -> api.chatMessageDelivered(ChatDemo.chatID, ChatDemo.lastMessageID) { r -> resultProcessing(r) }
                    "chatMessageRead" -> api.chatMessageRead(ChatDemo.chatID, ChatDemo.lastMessageID) { r -> resultProcessing(r) }
                    "chatTyping" -> api.chatTyping(ChatDemo.chatID) { r -> resultProcessing(r) }
                    "chatNotTyping" -> api.chatNotTyping(ChatDemo.chatID) { r -> resultProcessing(r) }
                    "disconnectChat" -> api.disconnectChat(ChatDemo.chatID) { r -> resultProcessing(r) }
                    "endChat" -> api.endChat(ChatDemo.chatID) { r -> resultProcessing(r) }
                    else -> Log.e("EEEEE", "########################################################")
                }

                api.callback = object : ContactCenterEventsInterface {
                    override fun chatSessionEvents(result: Result<List<ContactCenterEvent>, Error>) {
                        Log.e("&&&&&&&&&&&&", " &&&&&&&&&&&&&&&&&&&&&&&&&&& \t\n\t $result")
                        this@TestActivity.resultProcessing(result)
                    }
                }
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

                (result.value as? List<*>)?.firstOrNull {
                    (it as? ContactCenterEvent.ChatSessionMessage) != null
                }?.let {
                    (it as ContactCenterEvent.ChatSessionMessage)
                    Log.e("TestActivity", "MessageId = ${it.messageID}")
                    ChatDemo.lastMessageID = it.messageID
                }

                (result.value as? ContactCenterChatSessionProperties)?.let {
                    ChatDemo.chatID = it.chatID
                    ChatDemo.partyID = it.chatID
                    adapter.notifyDataSetChanged()
                    btnRunMessaging.isEnabled = ChatDemo.chatID.isNotEmpty()
                }
            }
        }
    }
}