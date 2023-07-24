package com.stfalcon.chatkit.utils

import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.messages.MessagesListAdapter.Wrapper

object ListUtils {
    fun <MESSAGE : Wrapper<Any>> findSmallItemIndex(source: List<MESSAGE>, item: IMessage): Int {
        return source.indexOfFirst {
            if (it.item is IMessage) {
                (it.item as IMessage).createdAt.time > item.createdAt.time
            } else false
        }
    }

    fun <MESSAGE : Wrapper<*>> latestMessage(source: List<MESSAGE>): IMessage? {
        return source.find { it.item is IMessage }?.item as? IMessage
    }

    fun <MESSAGE : Wrapper<*>> hasItemSmallerDateInMiddle(
        source: List<MESSAGE>,
        item: IMessage
    ): Boolean {
        val latestMessage = latestMessage(source)
        val latestMessagePos = source.indexOfFirst {
            if (it.item is IMessage) {
                (it.item as IMessage).id == latestMessage?.id
            } else false
        }


        return item.createdAt.time < (latestMessage(
            source
        )?.createdAt?.time ?: 0L)
    }
}
