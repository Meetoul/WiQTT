package com.example.wiqtt

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View;
import androidx.recyclerview.widget.RecyclerView
import com.example.wiqtt.mqtt.entities.Message

class MessageListAdapter(
    private var mMessageList: MutableList<Message>,
    private val mClickListener: (Message, Boolean) -> Unit
) :
    RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>() {

    inner class MessageViewHolder : RecyclerView.ViewHolder, View.OnClickListener, View.OnLongClickListener {
        private val mNameTextView: TextView

        constructor(view: View) : super(view) {
            mNameTextView = view.findViewById(R.id.message_list_item_name)
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) = mClickListener(mMessageList[adapterPosition], true)

        override fun onLongClick(p0: View?): Boolean {
            mClickListener(mMessageList[adapterPosition], false)
            return true
        }

        fun setName(name: String) {
            mNameTextView.text = name
        }
    }

    fun reloadMessages(messageList: MutableList<Message>) {
        mMessageList = messageList
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val listItemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_list_item, parent, false) as LinearLayout

        return MessageViewHolder(listItemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.setName(mMessageList[position].name)
    }

    override fun getItemCount() = mMessageList.size
}