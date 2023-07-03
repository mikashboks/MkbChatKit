package com.stfalcon.chatkit.sample.features.demo.custom.holder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.stfalcon.chatkit.sample.features.demo.custom.holder.holders.messages.CustomIncomingImageMessageViewHolder
import com.stfalcon.chatkit.sample.features.demo.custom.holder.holders.messages.CustomIncomingTextMessageViewHolder
import com.stfalcon.chatkit.sample.features.demo.custom.holder.holders.messages.CustomIncomingTextMessageViewHolder.OnAvatarClickListener
import com.stfalcon.chatkit.sample.features.demo.custom.holder.holders.messages.CustomIncomingTextMessageViewHolder.Payload
import com.stfalcon.chatkit.sample.features.demo.custom.holder.holders.messages.CustomOutcomingImageMessageViewHolder
import com.stfalcon.chatkit.sample.features.demo.custom.holder.holders.messages.CustomOutcomingTextMessageViewHolder
import com.stfalcon.chatkit.sample.utils.AppUtils

class CustomHolderMessagesActivity : DemoMessagesActivity(),
    OnMessageLongClickListener<Message?>, InputListener,
    AttachmentsListener {
    private var messagesList: MessagesList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_holder_messages)
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

        //We can pass any data to ViewHolder with payload
        val payload = Payload()
        //For example click listener
        payload.avatarClickListener =
            OnAvatarClickListener {
                Toast.makeText(
                    this@CustomHolderMessagesActivity,
                    "Text message avatar clicked",
                    Toast.LENGTH_SHORT
                ).show()
            }
        val holdersConfig = MessageHolders()
            .setIncomingTextConfig(
                CustomIncomingTextMessageViewHolder::class.java,
                R.layout.item_custom_incoming_text_message,
                payload
            )
            .setOutcomingTextConfig(
                CustomOutcomingTextMessageViewHolder::class.java,
                R.layout.item_custom_outcoming_text_message
            )
            .setIncomingImageConfig(
                CustomIncomingImageMessageViewHolder::class.java,
                R.layout.item_custom_incoming_image_message
            )
            .setOutcomingImageConfig(
                CustomOutcomingImageMessageViewHolder::class.java,
                R.layout.item_custom_outcoming_image_message
            )
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
                    CustomHolderMessagesActivity::class.java
                )
            )
        }
    }
}