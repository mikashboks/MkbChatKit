package com.stfalcon.chatkit.sample.features.demo.custom.layout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import com.stfalcon.chatkit.sample.R
import com.stfalcon.chatkit.sample.common.data.fixtures.DialogsFixtures
import com.stfalcon.chatkit.sample.common.data.model.Dialog
import com.stfalcon.chatkit.sample.features.demo.DemoDialogsActivity

class CustomLayoutDialogsActivity : DemoDialogsActivity() {
    private var dialogsList: DialogsList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_layout_dialogs)
        dialogsList = findViewById(R.id.dialogsList)
        initAdapter()
    }

    override fun onDialogClick(dialog: Dialog?) {
        CustomLayoutMessagesActivity.open(this)
    }

    private fun initAdapter() {
        super.dialogsAdapter = DialogsListAdapter(
            R.layout.item_custom_dialog,
            super.imageLoader
        )
        super.dialogsAdapter?.setItems(DialogsFixtures.getDialogs())
        super.dialogsAdapter?.setOnDialogClickListener(this)
        super.dialogsAdapter?.setOnDialogLongClickListener(
            this
        )
        dialogsList!!.setAdapter(super.dialogsAdapter)
    }

    companion object {
        fun open(context: Context) {
            context.startActivity(
                Intent(
                    context,
                    CustomLayoutDialogsActivity::class.java
                )
            )
        }
    }
}