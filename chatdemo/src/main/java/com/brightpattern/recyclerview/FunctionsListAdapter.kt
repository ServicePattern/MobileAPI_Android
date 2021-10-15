package com.brightpattern.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.brightpattern.ChatDemo
import com.brightpattern.chatdemo.R

class FunctionsListAdapter : RecyclerView.Adapter<FunctionsListAdapter.MyViewHolder>() {

    private val functionList = listOf(
            "getVersion",
            "checkAvailability",
            "requestChat",
            "endChat",
            "subscribeForRemoteNotificationsFirebase",
            "getChatHistory",
            "getCaseHistory",
            "closeCase",
            "sendChatMessage",
            "chatMessageDelivered",
            "chatMessageRead",
            "chatTyping",
            "chatNotTyping",
            "disconnectChat")

    private val alwaysActive = listOf("getVersion", "checkAvailability", "requestChat")

    var selection: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val context: Context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        return MyViewHolder(layoutInflater.inflate(R.layout.item_command, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cmdName = functionList[position]
        holder.cmdName.text = cmdName
        holder.cmdName.setOnClickListener {
            selection?.invoke(cmdName)
        }
        holder.cmdName.isEnabled = if (alwaysActive.contains(cmdName)) true else {
            ChatDemo.chatID.isNotEmpty()
        }
    }

    override fun getItemCount(): Int {
        return functionList.count()
    }

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val cmdName: Button = v.findViewById(R.id.btnCmdName)
    }

}