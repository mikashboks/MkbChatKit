package com.stfalcon.chatkit.sample.features.demo.styled

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import com.stfalcon.chatkit.sample.R
import com.stfalcon.chatkit.sample.common.data.fixtures.DialogsFixtures
import com.stfalcon.chatkit.sample.common.data.model.Dialog
import com.stfalcon.chatkit.sample.features.demo.DemoDialogsActivity
import com.stfalcon.chatkit.utils.DateFormatter
import java.util.Date

class StyledDialogsActivity : DemoDialogsActivity(),
    DateFormatter.Formatter {
    private var dialogsList: DialogsList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_styled_dialogs)
        dialogsList = findViewById(R.id.dialogsList)
        initAdapter()
    }

    override fun onDialogClick(dialog: Dialog?) {
        StyledMessagesActivity.open(this)
    }

    override fun format(date: Date): String {
        return if (DateFormatter.isToday(
                date
            )
        ) {
            DateFormatter.format(
                date,
                DateFormatter.Template.TIME
            )
        } else if (DateFormatter.isYesterday(
                date
            )
        ) {
            getString(R.string.date_header_yesterday)
        } else if (DateFormatter.isCurrentYear(
                date
            )
        ) {
            DateFormatter.format(
                date,
                DateFormatter.Template.STRING_DAY_MONTH
            )
        } else {
            DateFormatter.format(
                date,
                DateFormatter.Template.STRING_DAY_MONTH_YEAR
            )
        }
    }

    private fun initAdapter() {
        super.dialogsAdapter =
            DialogsListAdapter(super.imageLoader)
        super.dialogsAdapter?.setItems(DialogsFixtures.getDialogs())
        super.dialogsAdapter?.setOnDialogClickListener(this)
        super.dialogsAdapter?.setOnDialogLongClickListener(
            this
        )
        super.dialogsAdapter?.setDatesFormatter(this)
        dialogsList!!.setAdapter(super.dialogsAdapter)
    }

    companion object {
        fun open(context: Context) {
            context.startActivity(
                Intent(
                    context,
                    StyledDialogsActivity::class.java
                )
            )
        }
    }
}