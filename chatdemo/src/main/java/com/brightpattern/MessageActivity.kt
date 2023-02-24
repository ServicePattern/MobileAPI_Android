package com.brightpattern

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.utils.Result
import com.brightpattern.bpcontactcenter.utils.Success
import com.brightpattern.chatdemo.R
import com.brightpattern.customview.CustomIncomingTextMessageViewHolder
import com.brightpattern.recyclerview.ImagesAdapter
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import com.stfalcon.chatkit.commons.models.MessageContentType
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessageInput.TypingListener
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import java.util.*


class MessageActivity : AppCompatActivity() {

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
        private const val LOG_TAG = "MessageActivity"
    }


    private val api: ContactCenterCommunicator?
        get() {
            return ChatDemo.api
        }

    private val messagesList: MessagesList by lazy {
        findViewById(R.id.messagesList)
    }

    private val messageInput: MessageInput by lazy {
        findViewById(R.id.input)
    }

    private val imagesListView: RecyclerView by lazy {
        findViewById(R.id.imagesList)
    }

    private var images: MutableList<Uri> = mutableListOf()

    private val swipeHandler: ItemTouchHelper.SimpleCallback by lazy {
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT
        ) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                images.removeAt(viewHolder.bindingAdapterPosition)
                sendingImagesListAdapter.removedAt(viewHolder.bindingAdapterPosition)
                imageListUpdate()
            }

            override fun isItemViewSwipeEnabled(): Boolean {
                return true
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val swipeFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                return makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, swipeFlags)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
    }

    private fun imageListUpdate() {
        imagesListView.visibility = if (images.isNotEmpty()) View.VISIBLE else View.GONE
        messageInput.button.isEnabled = images.isNotEmpty() or messageInput.inputEditText.text.isNotEmpty()
    }

    private val touchHelper: ItemTouchHelper by lazy {
        ItemTouchHelper(swipeHandler)
    }

    private var imageLoader: ImageLoader = ImageLoader { imageView, url, _ -> imageView?.load(url) }

    private val myUser = MyUser(ChatDemo.chatID, "Me")
    private val systemUser = MyUser(UUID.randomUUID().toString(), "")

    private val parties = HashMap<String, IUser>().apply {
        this[myUser.userId] = myUser
    }

    private fun getParty(id: String?): IUser {
        return parties[id] ?: systemUser
    }

    private val messageListAdapter: MessagesListAdapter<MyMessage> by lazy {
        val holdersConfig = MessageHolders()
            .setIncomingTextConfig(
                CustomIncomingTextMessageViewHolder::class.java,
                R.layout.item_custom_incoming_text_message
            )

        MessagesListAdapter<MyMessage>(ChatDemo.chatID, holdersConfig, imageLoader)
    }

    private val sendingImagesListAdapter: ImagesAdapter by lazy {
        ImagesAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        messagesList.setAdapter(messageListAdapter)

        imagesListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesListView.adapter = sendingImagesListAdapter
        touchHelper.attachToRecyclerView(imagesListView)

    }

    override fun onPause() {
        Log.e(LOG_TAG, "************** onPause **************")
        api?.let { api ->
            api.stopPolling(ChatDemo.chatID) { r ->
                if (r is Success) {
                    Log.d(LOG_TAG, "stopped event polling")
                }
            }
        }
        super.onPause()
    }

    override fun onResume() {
        Log.e(LOG_TAG, "************** onResume **************")
        super.onResume()

        api?.let { api ->
            api.startPolling(ChatDemo.chatID) { r ->
                if (r is Success) {
                    Log.d(LOG_TAG, "started event polling")
                }
            }

            api.callback = object : ContactCenterEventsInterface {
                override fun chatSessionEvents(result: Result<List<ContactCenterEvent>, Error>) {
                    this@MessageActivity.resultProcessing(result)
                }
            }

            api.getChatHistory(ChatDemo.chatID) { r -> resultProcessing(r) }

            messageInput.setTypingListener(object : TypingListener {
                override fun onStartTyping() {}

                override fun onStopTyping() {
                    imageListUpdate()
                }

            })

            messageInput.setInputListener { messageText ->

                images.forEach { uri ->
                    val bitmap = applicationContext.contentResolver.openInputStream(uri).use { data ->
                        BitmapFactory.decodeStream(data)
                    }
                    api.uploadFile(uri.lastPathSegment ?: "unknown.png", bitmap) { result ->

                        if (result is Success) {
                            api.sendChatFile(ChatDemo.chatID, result.value.fileUUID, result.value.fileName, "image") { sendMessageResult ->

                                if (sendMessageResult is Success) {
                                    val myImageMessage = sendMessageResult.value.first().url?.let { it1 -> MyMessage("", it1, myUser) }
                                    messageListAdapter.addToStart(myImageMessage, true)

                                    images.removeAt(images.indexOf(uri))
                                    imageListUpdate()
                                }
                            }

                        }
                    }
                }

                val messageID = UUID.randomUUID()
                api.sendChatMessage(ChatDemo.chatID, "$messageText", messageID) { result ->
                    if (result is Success) {
                        val myMessage = MyMessage("$messageText", null, myUser, messageID.toString())
                        messageListAdapter.addToStart(myMessage, true)
                    }
                }
                return@setInputListener true
            }

            messageInput.setAttachmentsListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED
                    ) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, PERMISSION_CODE)
                    } else {
                        pickImageFromGallery()
                    }
                } else {
                    pickImageFromGallery()
                }
            }
        }
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            Log.d(LOG_TAG, "Image URI: ${data?.data}")
            data?.data?.let { uri ->
                images.add(uri)
                sendingImagesListAdapter.items = images
                imagesListView.visibility = View.VISIBLE
                messageInput.button.isEnabled = true
            }
        }
    }


    fun resultProcessing(result: Any) {
        if (result is Success<*>) {
            @Suppress("UNCHECKED_CAST")
            (result.value as? List<ContactCenterEvent>)?.forEach {
                (it as? ContactCenterEvent.ChatSessionMessage)?.let { message ->
                    val incomingMessage = MyMessage(message.message, null, getParty(message.partyID), message.messageID)
                    messageListAdapter.addToStart(incomingMessage, true)

                    api?.chatMessageDelivered(ChatDemo.chatID, message.messageID) { r ->
                        if (r is Success) {
                            Log.e("S_DELIVERED", "OK")
                        }
                    }

                    api?.chatMessageRead(ChatDemo.chatID, message.messageID) { r ->
                        if (r is Success) {
                            Log.e("S_READ", "OK")
                        }
                    }
                }
                (it as? ContactCenterEvent.ChatSessionFile)?.let { message ->
                    if (message.fileType == "image") {
                        val incomingMessage = message.url?.let { it1 -> MyMessage("", it1, getParty(message.partyID)) }
                        Log.e(LOG_TAG, " $incomingMessage")
                        messageListAdapter.addToStart(incomingMessage, true)
                    } else {
                        val incomingMessage = MyMessage("Unsupported ${message.fileType} file ${message.fileName}", null, getParty(message.partyID))
                        messageListAdapter.addToStart(incomingMessage, true)
                    }
                }
                (it as? ContactCenterEvent.ChatSessionPartyJoined)?.let { message ->
                    val user = MyUser(message.partyID, message.displayName)
                    parties[user.userId] = user
                    val incomingMessage = MyMessage("Joined the session", null, user)
                    messageListAdapter.addToStart(incomingMessage, true)
                }
                (it as? ContactCenterEvent.ChatSessionPartyLeft)?.let { message ->
                    val incomingMessage = MyMessage("Left the session", null, getParty(message.partyID))
                    messageListAdapter.addToStart(incomingMessage, true)
                }
                (it as? ContactCenterEvent.ChatSessionTimeoutWarning)?.let { message ->
                    val incomingMessage = MyMessage(message.message, null, systemUser)
                    messageListAdapter.addToStart(incomingMessage, true)
                }
                (it as? ContactCenterEvent.ChatSessionInactivityTimeout)?.let { message ->
                    val incomingMessage = MyMessage(message.message, null, systemUser)
                    messageListAdapter.addToStart(incomingMessage, true)
                }
                (it as? ContactCenterEvent.ChatSessionEnded)?.let { _ ->
                    val incomingMessage = MyMessage("The session has ended", null, systemUser)
                    messageListAdapter.addToStart(incomingMessage, true)

                    // Set the result and close the activity
                    setResult(ChatDemo.CLOSED_BY_SERVER)
                    finish()
                }
            }
        }
    }
}

data class MyMessage(val message: String, val url: String?, val messageUser: IUser, val messageID: String = "", val timestamp: Long = System.currentTimeMillis() / 1000) : IMessage,
    MessageContentType.Image {
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

    override fun getImageUrl(): String? {
        return url
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

