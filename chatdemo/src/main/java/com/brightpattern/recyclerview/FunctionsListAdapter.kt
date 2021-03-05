package com.brightpattern.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.brightpattern.chatdemo.R

class FunctionsListAdapter : RecyclerView.Adapter<FunctionsListAdapter.MyViewHolder>() {

    val functionList = listOf("checkAvailability", "requestChat", "getChatHistory",
            "getCaseHistory",
            "sendChatMessage",
            "chatMessageDelivered",
            "chatMessageRead",
            "chatTyping",
            "chatNotTyping",
            "disconnectChat",
            "endChat",
            "subscribeForRemoteNotificationsFirebase",
            "subscribeForRemoteNotificationsAPNs")

    var selection: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val context: Context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        return MyViewHolder(layoutInflater.inflate(R.layout.item_command, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.cmdName.text = functionList[position]
        holder.cmdName.setOnClickListener {
            selection?.invoke(functionList[position])
        }
    }

    override fun getItemCount(): Int {
        return functionList.count()
    }

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val cmdName = v.findViewById<Button>(R.id.btnCmdName)
    }

}