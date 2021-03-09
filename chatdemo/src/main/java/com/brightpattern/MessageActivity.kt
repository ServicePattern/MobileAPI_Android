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
import java.time.Instant
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

    val myUser = MyUser(ChatDemo.chatID, "Me")
    val systemUser = MyUser(UUID.randomUUID().toString(), "")

    private val parties = HashMap<String, MyUser>().apply {
        this[myUser.userId] = myUser
    }

    private fun getParty(id: String?) : IUser {
        return parties[id] ?: systemUser
    }

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

        api.getChatHistory(ChatDemo.chatID) { r -> resultProcessing(r) }

        messageInput.setInputListener { messageText ->
            val messageID = UUID.randomUUID()
            api.sendChatMessage(ChatDemo.chatID, "$messageText", messageID) { result ->
                if (result is Success) {
                    val myMessage = MyMessage("$messageText", myUser, messageID.toString())
                    messageListAdapter.addToStart(myMessage, true)
                }
            }
            return@setInputListener true
        }
    }

    fun resultProcessing(result: Any) {
        if (result is Success<*>) {
            (result.value as? List<ContactCenterEvent>)?.forEach {
                (it as? ContactCenterEvent.ChatSessionMessage)?.let { message ->
                    val incomingMessage = MyMessage(message.message, getParty(message.partyID), message.messageID)
                    messageListAdapter.addToStart(incomingMessage, true)
                }
                (it as? ContactCenterEvent.ChatSessionPartyJoined)?.let { message ->
                    val user = MyUser(message.partyID, message.displayName ?: ((message.firstName ?: "") + " " + (message.lastName ?: "")))
                    parties[user.userId] = user
                    val incomingMessage = MyMessage("Joined the session", user)
                    messageListAdapter.addToStart(incomingMessage, true)
                }
                (it as? ContactCenterEvent.ChatSessionPartyLeft)?.let { message ->
                    val incomingMessage = MyMessage("Left the session", getParty(message.partyID))
                    messageListAdapter.addToStart(incomingMessage, true)
                }
                (it as? ContactCenterEvent.ChatSessionTimeoutWarning)?.let { message ->
                    val incomingMessage = MyMessage(message.message, systemUser)
                    messageListAdapter.addToStart(incomingMessage, true)
                }
                (it as? ContactCenterEvent.ChatSessionInactivityTimeout)?.let { message ->
                    val incomingMessage = MyMessage(message.message, systemUser)
                    messageListAdapter.addToStart(incomingMessage, true)
                }
                (it as? ContactCenterEvent.ChatSessionEnded)?.let { _ ->
                    val incomingMessage = MyMessage("The session has ended", systemUser)
                    messageListAdapter.addToStart(incomingMessage, true)

                    // Set the result and close the activity
                    setResult(ChatDemo.CLOSED_BY_SERVER)
                    finish()
                }
            }
        }
    }
}

data class MyMessage(val message: String, val messageUser: IUser, val messageID: String = "", val timestamp: Long = Instant.now().epochSecond) : IMessage {
    override fun getId(): String {
        return messageID
    }

    override fun getText(): String {
        return message
    }

    override fun getUser(): IUser {
        return messageUser
    }

    override fun getCreatedAt(): Date {
        return Date(timestamp * 1000)
    }
}

data class MyUser(val userId: String, val displayName: String) : IUser {
    override fun getId(): String {
        return userId
    }

    override fun getName(): String {
        return displayName
    }

    override fun getAvatar(): String {
        return ""
    }

}