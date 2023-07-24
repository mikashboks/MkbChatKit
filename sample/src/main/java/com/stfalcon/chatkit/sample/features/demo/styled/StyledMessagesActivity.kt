package com.stfalcon.chatkit.sample.features.demo.styled

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessageInput.AttachmentsListener
import com.stfalcon.chatkit.messages.MessageInput.InputListener
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.stfalcon.chatkit.sample.R
import com.stfalcon.chatkit.sample.common.data.fixtures.MessagesFixtures
import com.stfalcon.chatkit.sample.features.demo.DemoMessagesActivity
import com.stfalcon.chatkit.utils.DateFormatter
import java.util.Date

class StyledMessagesActivity : DemoMessagesActivity(),
    InputListener, AttachmentsListener,
    DateFormatter.Formatter {
    private var messagesList: MessagesList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_styled_messages)
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

    override fun format(date: Date?): String {
        return if (DateFormatter.isToday(
                date
            )
        ) {
            getString(R.string.date_header_today)
        } else if (DateFormatter.isYesterday(
                date
            )
        ) {
            getString(R.string.date_header_yesterday)
        } else {
            DateFormatter.format(
                date,
                DateFormatter.Template.STRING_DAY_MONTH_YEAR
            )
        }
    }

    private fun initAdapter() {
        super.messagesAdapter = MessagesListAdapter(
            super.senderId,
            super.imageLoader
        )
        super.messagesAdapter!!.enableSelectionMode(this)
        super.messagesAdapter!!.setLoadMoreListener(this)
        super.messagesAdapter!!.setDateHeadersFormatter(this)
        messagesList!!.setAdapter(super.messagesAdapter)
    }

    companion object {
        @JvmStatic
        fun open(context: Context) {
            context.startActivity(
                Intent(
                    context,
                    StyledMessagesActivity::class.java
                )
            )
        }
    }
}
