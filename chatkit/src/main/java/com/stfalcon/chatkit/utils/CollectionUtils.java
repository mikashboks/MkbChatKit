package com.stfalcon.chatkit.utils;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageUnread;
import com.stfalcon.chatkit.messages.MessagesListAdapter.Wrapper;

import java.util.Iterator;
import java.util.List;

import kotlin.jvm.internal.Intrinsics;

public class CollectionUtils {

    public static <DATA> boolean hasUnreadMessages(List<Wrapper<DATA>> items) {
        boolean hasUnread;
        if (items.isEmpty()) {
            hasUnread = false;
        } else {
            Iterator<Wrapper<DATA>> ii = items.iterator();
            while (true) {
                if (!ii.hasNext()) {
                    hasUnread = false;
                    break;
                }
                if (ii.next().getItem() instanceof MessageUnread) {
                    hasUnread = true;
                    break;
                }
            }
        }

        return hasUnread;
    }

    public static <DATA> int countUnreadMessages(List<Wrapper<DATA>> items) {
        Intrinsics.checkNotNullParameter(items, "items");
        int counter;
        if (items.isEmpty()) {
            counter = 0;
        } else {
            int count$iv = 0;

            for (Wrapper<DATA> it : items) {
                boolean var9;
                if (it.getItem() instanceof IMessage) {
                    Object iMessage = it.getItem();
                    var9 = ((IMessage) iMessage).isUnread();
                } else {
                    var9 = false;
                }

                if (var9) {
                    ++count$iv;
                    if (count$iv < 0) {
                        throw new ArithmeticException("Count overflow has happened.");
                    }
                }
            }

            counter = count$iv;
        }

        return counter;
    }
}
