package com.stfalcon.chatkit.sample.features.demo.custom.layout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessageInput.AttachmentsListener
import com.stfalcon.chatkit.messages.MessageInput.InputListener
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.stfalcon.chatkit.messages.MessagesListAdapter.OnMessageLongClickListener
import com.stfalcon.chatkit.sample.R
import com.stfalcon.chatkit.sample.common.data.fixtures.MessagesFixtures
import com.stfalcon.chatkit.sample.common.data.model.Message
import com.stfalcon.chatkit.sample.features.demo.DemoMessagesActivity
import com.stfalcon.chatkit.sample.utils.AppUtils

class CustomLayoutMessagesActivity : DemoMessagesActivity(),
    OnMessageLongClickListener<Message?>, InputListener,
    AttachmentsListener {
    private var messagesList: MessagesList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_layout_messages)
        messagesList = findViewById(R.id.messagesList)
        initAdapter()
        val input = findViewById<MessageInput>(R.id.input)
        input.setInputListener(this)
        input.setAttachmentsListener(this)
    }

    override fun onSubmit(input: CharSequence): Boolean {
        messagesAdapter!!.addToStart(
            MessagesFixtures.getTextMessage(input.toString()),
            true
        )
        return true
    }

    override fun onAddAttachments() {
        messagesAdapter!!.addToStart(
            MessagesFixtures.getImageMessage(),
            true
        )
    }

    override fun onMessageLongClick(message: Message?) {
        AppUtils.showToast(
            this,
            R.string.on_log_click_message,
            false
        )
    }

    private fun initAdapter() {
        val holdersConfig = MessageHolders()
            .setIncomingTextLayout(R.layout.item_custom_incoming_text_message)
            .setOutcomingTextLayout(R.layout.item_custom_outcoming_text_message)
            .setIncomingImageLayout(R.layout.item_custom_incoming_image_message)
            .setOutcomingImageLayout(R.layout.item_custom_outcoming_image_message)
        super.messagesAdapter = MessagesListAdapter(
            super.senderId,
            holdersConfig,
            super.imageLoader
        )
        super.messagesAdapter!!.setOnMessageLongClickListener(
            this
        )
        super.messagesAdapter!!.setLoadMoreListener(this)
        messagesList!!.setAdapter(super.messagesAdapter)
    }

    companion object {
        @JvmStatic
        fun open(context: Context) {
            context.startActivity(
                Intent(
                    context,
                    CustomLayoutMessagesActivity::class.java
                )
            )
        }
    }
}