package com.stfalcon.chatkit.sample.features.demo.def

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessageInput.AttachmentsListener
import com.stfalcon.chatkit.messages.MessageInput.InputListener
import com.stfalcon.chatkit.messages.MessageInput.TypingListener
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.stfalcon.chatkit.sample.R
import com.stfalcon.chatkit.sample.common.data.fixtures.MessagesFixtures
import com.stfalcon.chatkit.sample.common.data.model.Message
import com.stfalcon.chatkit.sample.features.demo.DemoMessagesActivity
import com.stfalcon.chatkit.sample.utils.AppUtils

class DefaultMessagesActivity : DemoMessagesActivity(),
    InputListener, AttachmentsListener, TypingListener {
    private var messagesList: MessagesList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_messages)
        messagesList = findViewById(R.id.messagesList)
        initAdapter()
        val input = findViewById<MessageInput>(R.id.input)
        input.setInputListener(this)
        input.setTypingListener(this)
        input.setAttachmentsListener(this)
    }

    override fun onSubmit(input: CharSequence): Boolean {
        super.messagesAdapter!!.addToStart(
            MessagesFixtures.getTextMessage(input.toString()),
            true
        )
        return true
    }

    override fun onAddAttachments() {
        super.messagesAdapter!!.addToStart(
            MessagesFixtures.getImageMessage(), true
        )
    }

    private fun initAdapter() {
        super.messagesAdapter = MessagesListAdapter(
            super.senderId,
            super.imageLoader
        )
        super.messagesAdapter!!.enableSelectionMode(this)
        super.messagesAdapter!!.setLoadMoreListener(this)
        super.messagesAdapter!!.registerViewClickListener(
            com.mikashboks.chatkit.R.id.messageUserAvatar
        ) { view: View?, message: Message ->
            AppUtils.showToast(
                this@DefaultMessagesActivity,
                message.user.name + " avatar click",
                false
            )
        }
        messagesList!!.setAdapter(super.messagesAdapter)
    }

    override fun onStartTyping() {
        Log.v(
            "Typing listener",
            getString(R.string.start_typing_status)
        )
    }

    override fun onStopTyping() {
        Log.v(
            "Typing listener",
            getString(R.string.stop_typing_status)
        )
    }

    companion object {
        @JvmStatic
        fun open(context: Context) {
            context.startActivity(
                Intent(
                    context,
                    DefaultMessagesActivity::class.java
                )
            )
        }
    }
}