package com.example.wiqtt

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wiqtt.mqtt.entities.Message

class MessageListAdapter(private val mMessageList: MutableList<Message>) :
    RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>() {
    class MessageViewHolder(val nameTextView: TextView) : RecyclerView.ViewHolder(nameTextView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val listItemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_list_item, parent, false) as LinearLayout
        val textView: TextView = listItemView.findViewById(R.id.message_list_item_name)

        return MessageViewHolder(textView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.nameTextView.text = mMessageList[position].name
    }

    override fun getItemCount() = mMessageList.size
}