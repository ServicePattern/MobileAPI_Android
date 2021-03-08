package com.brightpattern

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.utils.Result
import com.brightpattern.bpcontactcenter.utils.Success
import com.brightpattern.chatdemo.R
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import java.util.*

class MessageActivity : AppCompatActivity() {

    private val api: ContactCenterCommunicator by lazy {
        ChatDemo.api
    }

    private val messagesList: MessagesList by lazy {
        findViewById(R.id.messagesList)
    }

    private val messageInput: MessageInput by lazy {
        findViewById(R.id.input)
    }

    var imageLoader: ImageLoader = ImageLoader { imageView, url, _ -> imageView?.load(url) }

    private val messageListAdapter: MessagesListAdapter<MyMessage> by lazy {
        MessagesListAdapter<MyMessage>(ChatDemo.chatID, imageLoader)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        messagesList.setAdapter(messageListAdapter)
    }

    override fun onResume() {
        Log.e("MessageActivity", "************** onResume **************")
        super.onResume()
        api.callback = object : ContactCenterEventsInterface {
            override fun chatSessionEvents(result: Result<List<ContactCenterEvent>, Error>) {
                Log.e("&&&&&&&&&&&&", " &&&&&&&&&&&&&&&&&&&&&&&&&&& \t\n\t $result")
                this@MessageActivity.resultProcessing(result)
            }
        }

        api.getCaseHistory(ChatDemo.chatID) { r -> resultProcessing(r) }

        messageInput.setInputListener { messageText ->
            val messageID = UUID.randomUUID()
            api.sendChatMessage(ChatDemo.chatID, "$messageText", messageID) { result ->
                if (result is Success) {
                    val myMessage = MyMessage(ContactCenterEvent.ChatSessionMessage(messageID.toString(), null, "$messageText"))
                    messageListAdapter.addToStart(myMessage, true)
                }
            }
            return@setInputListener true
        }
    }

    fun resultProcessing(result: Any) {
        if (result is Success<*>) {
            (result.value as? List<ContactCenterEvent>)?.filter { (it as? ContactCenterEvent.ChatSessionMessage) != null }?.forEach {
                (it as? ContactCenterEvent.ChatSessionMessage)?.let { message ->
                    val incomingMessage = MyMessage(message)
                    messageListAdapter.addToStart(incomingMessage, true)
                }
            }
        }
    }
}

data class MyMessage(val message: ContactCenterEvent.ChatSessionMessage) : IMessage {
    override fun getId(): String {
        return message.messageID
    }

    override fun getText(): String {
        return message.message
    }

    override fun getUser(): IUser {
        return object : IUser {
            override fun getId(): String {
                return UUID.randomUUID().toString()
            }

            override fun getName(): String {
                return "User Name"
            }

            override fun getAvatar(): String {
                return ""
            }

        }
    }

    override fun getCreatedAt(): Date {
        return Date(message.timestamp * 1000)
    }

}