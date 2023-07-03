package com.stfalcon.chatkit.sample.features.demo

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import com.stfalcon.chatkit.dialogs.DialogsListAdapter.OnDialogClickListener
import com.stfalcon.chatkit.dialogs.DialogsListAdapter.OnDialogLongClickListener
import com.stfalcon.chatkit.sample.common.data.model.Dialog
import com.stfalcon.chatkit.sample.utils.AppUtils

/*
 * Created by troy379 on 05.04.17.
 */
abstract class DemoDialogsActivity : AppCompatActivity(),
    OnDialogClickListener<Dialog?>,
    OnDialogLongClickListener<Dialog?> {
    protected var imageLoader: ImageLoader? = null
    protected var dialogsAdapter: DialogsListAdapter<Dialog>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLoader =
            ImageLoader { imageView: ImageView?, url: String?, payload: Any? ->
                Picasso.get().load(url).into(imageView)
            }
    }

    override fun onDialogLongClick(dialog: Dialog?) {
        AppUtils.showToast(
            this,
            dialog?.dialogName,
            false
        )
    }
}