package com.stfalcon.chatkit.sample.features.demo.custom.holder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import com.stfalcon.chatkit.sample.R
import com.stfalcon.chatkit.sample.common.data.fixtures.DialogsFixtures
import com.stfalcon.chatkit.sample.common.data.model.Dialog
import com.stfalcon.chatkit.sample.features.demo.DemoDialogsActivity
import com.stfalcon.chatkit.sample.features.demo.custom.holder.holders.dialogs.CustomDialogViewHolder

class CustomHolderDialogsActivity : DemoDialogsActivity() {
    private var dialogsList: DialogsList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_holder_dialogs)
        dialogsList = findViewById(R.id.dialogsList)
        initAdapter()
    }

    override fun onDialogClick(dialog: Dialog?) {
        CustomHolderMessagesActivity.open(this)
    }

    private fun initAdapter() {
        super.dialogsAdapter = DialogsListAdapter(
            R.layout.item_custom_dialog_view_holder,
            CustomDialogViewHolder::class.java,
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
                    CustomHolderDialogsActivity::class.java
                )
            )
        }
    }
}