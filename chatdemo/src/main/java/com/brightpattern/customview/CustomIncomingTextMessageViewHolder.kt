package com.brightpattern.customview

import android.view.View
import android.widget.TextView
import com.brightpattern.MyMessage
import com.brightpattern.MyUser
import com.brightpattern.chatdemo.R
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder

class CustomIncomingTextMessageViewHolder(itemView: View) : IncomingTextMessageViewHolder<MyMessage>(itemView, null) {

    override fun onBind(message: MyMessage) {
        super.onBind(message)
        itemView.findViewById<TextView>(R.id.tvDisplayName)?.let {
            it.text = (message.messageUser as? MyUser)?.displayName
        }
    }
}
