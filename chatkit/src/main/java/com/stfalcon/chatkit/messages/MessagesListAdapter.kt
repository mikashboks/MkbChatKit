/*******************************************************************************
 * Copyright 2016 stfalcon.com
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stfalcon.chatkit.messages

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.util.SparseArray
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.mikashboks.chatkit.R
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.commons.ViewHolder
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.MessageUnread
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageHolders.DefaultMessageViewHolder
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder
import com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder
import com.stfalcon.chatkit.utils.CollectionUtils
import com.stfalcon.chatkit.utils.DateFormatter
import com.stfalcon.chatkit.utils.ListUtils
import java.util.Collections
import java.util.Date

/**
 * Adapter for [MessagesList].
 */
open class MessagesListAdapter<MESSAGE : IMessage?>(
    private val senderId: String?,
    private val holders: MessageHolders,
    private val imageLoader: ImageLoader?
) : RecyclerView.Adapter<ViewHolder<*>>(),
    RecyclerScrollMoreListener.OnLoadMoreListener {

    private val items: MutableList<Wrapper<Any>>
    private var selectedItemsCount = 0
    private var selectionListener: SelectionListener? = null
    private var loadMoreListener: OnLoadMoreListener? = null
    private var onMessageClickListener: OnMessageClickListener<MESSAGE>? =
        null
    private var onMessageViewClickListener: OnMessageViewClickListener<MESSAGE>? =
        null
    private var onMessageLongClickListener: OnMessageLongClickListener<MESSAGE>? =
        null
    private var onMessageViewLongClickListener: OnMessageViewLongClickListener<MESSAGE>? =
        null
    private var layoutManager: RecyclerView.LayoutManager? =
        null
    private var messagesListStyle: MessagesListStyle? = null
    private var dateHeadersFormatter: DateFormatter.Formatter? =
        null
    private val viewClickListenersArray =
        SparseArray<OnMessageViewClickListener<*>>()

    /**
     * For default list item layout and view holder.
     *
     * @param senderId    identifier of sender.
     * @param imageLoader image loading method.
     */
    constructor(
        senderId: String?,
        imageLoader: ImageLoader?
    ) : this(senderId, MessageHolders(), imageLoader) {
    }

    /**
     * For default list item layout and view holder.
     *
     * @param senderId    identifier of sender.
     * @param holders     custom layouts and view holders. See [MessageHolders] documentation for details
     * @param imageLoader image loading method.
     */
    init {
        items = ArrayList()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<*> {
        return holders.getHolder(
            parent,
            viewType,
            messagesListStyle
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder<*>,
        position: Int
    ) {
        val wrapper: Wrapper<*> = items!![position]
        holders.bind(
            holder,
            wrapper.item,
            wrapper.isSelected,
            imageLoader,
            getMessageClickListener(wrapper),
            getMessageLongClickListener(wrapper),
            dateHeadersFormatter,
            viewClickListenersArray
        )
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return holders.getViewType(
            items!![position].item,
            senderId
        )
    }

    override fun onLoadMore(page: Int, total: Int) {
        if (loadMoreListener != null) {
            loadMoreListener!!.onLoadMore(page, total)
        }
    }

    override fun getMessagesCount(): Int {
        var count = 0
        for (item in items) {
            if (item.item is IMessage) {
                count++
            }
        }
        return count
    }

    val messages: List<Wrapper<Any>>
        get() = items
    /*
     * PUBLIC METHODS
     * */
    /**
     * Adds message to bottom of list and scroll if needed.
     *
     * @param message message to add.
     * @param scroll  `true` if need to scroll list to bottom when message added.
     */
    fun addToStart(message: MESSAGE, scroll: Boolean) {
        val isNewMessageToday =
            !isPreviousSameDate(0, message!!.createdAt)
        if (isNewMessageToday) {
            items.add(
                0, Wrapper(
                message.createdAt
            )
            )
        }
        val element = Wrapper<Any>(message)
        items.add(0, element)
        notifyItemRangeInserted(
            0,
            if (isNewMessageToday) 2 else 1
        )
        if (layoutManager != null && scroll) {
            layoutManager!!.scrollToPosition(0)
        }
    }

    /**
     * Adds messages list in chronological order. Use this method to add history.
     *
     * @param messages messages from history.
     * @param reverse  `true` if need to reverse messages before adding.
     */
    fun addToEnd(messages: List<MESSAGE>, reverse: Boolean) {
        if (messages.isEmpty()) return
        val sortedMessage = messages.sortedBy { it?.createdAt?.time }
        if (reverse) Collections.reverse(sortedMessage)
        if (items.isNotEmpty()) {
            val lastItemPosition = items.size - 1
            val lastItem = items[lastItemPosition].item as Date?
            if (DateFormatter.isSameDay(sortedMessage[0]!!.createdAt, lastItem)) {
                items.removeAt(lastItemPosition)
                notifyItemRemoved(lastItemPosition)
            }
        }
        val oldSize = items.size
        generateHeaders(sortedMessage)
        notifyItemRangeInserted(oldSize, items.size - oldSize)
    }

    /**
     * Updates message by its id.
     *
     * @param message updated message object.
     */
    fun update(message: MESSAGE): Boolean {
        return update(message!!.id, message)
    }

    /**
     * Updates message by old identifier (use this method if id has changed). Otherwise use [.update]
     *
     * @param oldId      an identifier of message to update.
     * @param newMessage new message object.
     */
    fun update(
        oldId: String,
        newMessage: MESSAGE
    ): Boolean {
        val position = getMessagePositionById(oldId)
        return if (position >= 0) {
            val element = Wrapper<Any>(newMessage!!)
            items[position] = element
            notifyItemChanged(position)
            true
        } else {
            false
        }
    }

    /**
     * Moves the elements position from current to start
     *
     * @param newMessage new message object.
     */
    fun updateAndMoveToStart(newMessage: MESSAGE) {
        val position = getMessagePositionById(
            newMessage!!.id
        )
        if (position >= 0) {
            val element = Wrapper<Any>(newMessage)
            items.removeAt(position)
            items.add(0, element)
            notifyItemMoved(position, 0)
            notifyItemChanged(0)
        }
    }

    /**
     * Updates message by its id if it exists, add to start if not
     *
     * @param message message object to insert or update.
     */
    fun upsert(message: MESSAGE) {
        if (!update(message)) {
            addToStart(message, false)
        }
    }

    /**
     * Updates and moves to start if message by its id exists and if specified move to start, if not
     * specified the item stays at current position and updated
     *
     * @param message message object to insert or update.
     */
    fun upsert(
        message: MESSAGE,
        moveToStartIfUpdate: Boolean
    ) {
        if (moveToStartIfUpdate) {
            if (getMessagePositionById(message!!.id) > 0) {
                updateAndMoveToStart(message)
            } else {
                upsert(message)
            }
        } else {
            upsert(message)
        }
    }

    /**
     * Deletes message.
     *
     * @param message message to delete.
     */
    fun delete(message: MESSAGE) {
        deleteById(message!!.id)
    }

    /**
     * Deletes messages list.
     *
     * @param messages messages list to delete.
     */
    fun delete(messages: List<MESSAGE?>) {
        var result = false
        for (message in messages) {
            val index = getMessagePositionById(
                message!!.id
            )
            if (index >= 0) {
                items!!.removeAt(index)
                notifyItemRemoved(index)
                result = true
            }
        }
        if (result) {
            recountDateHeaders()
        }
    }

    /**
     * Deletes message by its identifier.
     *
     * @param id identifier of message to delete.
     */
    fun deleteById(id: String) {
        val index = getMessagePositionById(id)
        if (index >= 0) {
            items!!.removeAt(index)
            notifyItemRemoved(index)
            recountDateHeaders()
        }
    }

    /**
     * Deletes messages by its identifiers.
     *
     * @param ids array of identifiers of messages to delete.
     */
    fun deleteByIds(ids: Array<String>) {
        var result = false
        for (id in ids) {
            val index = getMessagePositionById(id)
            if (index >= 0) {
                items!!.removeAt(index)
                notifyItemRemoved(index)
                result = true
            }
        }
        if (result) {
            recountDateHeaders()
        }
    }

    /**
     * Returns `true` if, and only if, messages count in adapter is non-zero.
     *
     * @return `true` if size is 0, otherwise `false`
     */
    val isEmpty: Boolean
        get() = items!!.isEmpty()
    /**
     * Clears the messages list.
     */
    /**
     * Clears the messages list. With notifyDataSetChanged
     */
    @JvmOverloads
    fun clear(notifyDataSetChanged: Boolean = true) {
        if (items != null) {
            items.clear()
            if (notifyDataSetChanged) {
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Enables selection mode.
     *
     * @param selectionListener listener for selected items count. To get selected messages use [.getSelectedMessages].
     */
    fun enableSelectionMode(selectionListener: SelectionListener?) {
        requireNotNull(selectionListener) { "SelectionListener must not be null. Use `disableSelectionMode()` if you want tp disable selection mode" }
        this.selectionListener = selectionListener
    }

    /**
     * Disables selection mode and removes [SelectionListener].
     */
    fun disableSelectionMode() {
        selectionListener = null
        unselectAllItems()
    }

    /**
     * Returns the list of selected messages.
     *
     * @return list of selected messages. Empty list if nothing was selected or selection mode is disabled.
     */
    val selectedMessages: ArrayList<MESSAGE?>
        get() {
            val selectedMessages = ArrayList<MESSAGE?>()
            for (wrapper in items!!) {
                if (wrapper.item is IMessage && wrapper.isSelected) {
                    selectedMessages.add(wrapper.item as MESSAGE)
                }
            }
            return selectedMessages
        }

    /**
     * Returns selected messages text and do [.unselectAllItems] for you.
     *
     * @param formatter The formatter that allows you to format your message model when copying.
     * @param reverse   Change ordering when copying messages.
     * @return formatted text by [Formatter]. If it's `null` - `MESSAGE#toString()` will be used.
     */
    fun getSelectedMessagesText(
        formatter: Formatter<MESSAGE?>?,
        reverse: Boolean
    ): String {
        val copiedText = getSelectedText(formatter, reverse)
        unselectAllItems()
        return copiedText
    }

    /**
     * Copies text to device clipboard and returns selected messages text. Also it does [.unselectAllItems] for you.
     *
     * @param context   The context.
     * @param formatter The formatter that allows you to format your message model when copying.
     * @param reverse   Change ordering when copying messages.
     * @return formatted text by [Formatter]. If it's `null` - `MESSAGE#toString()` will be used.
     */
    fun copySelectedMessagesText(
        context: Context,
        formatter: Formatter<MESSAGE?>?,
        reverse: Boolean
    ): String {
        val copiedText = getSelectedText(formatter, reverse)
        copyToClipboard(context, copiedText)
        unselectAllItems()
        return copiedText
    }

    /**
     * Unselect all of the selected messages. Notifies [SelectionListener] with zero count.
     */
    fun unselectAllItems() {
        for (i in items!!.indices) {
            val wrapper: Wrapper<*> = items[i]
            if (wrapper.isSelected) {
                wrapper.isSelected = false
                notifyItemChanged(i)
            }
        }
        isSelectionModeEnabled = false
        selectedItemsCount = 0
        notifySelectionChanged()
    }

    /**
     * Deletes all of the selected messages and disables selection mode.
     * Call [.getSelectedMessages] before calling this method to delete messages from your data source.
     */
    fun deleteSelectedMessages() {
        val selectedMessages: List<MESSAGE?> =
            selectedMessages
        delete(selectedMessages)
        unselectAllItems()
    }

    /**
     * Sets click listener for item. Fires ONLY if list is not in selection mode.
     *
     * @param onMessageClickListener click listener.
     */
    fun setOnMessageClickListener(onMessageClickListener: OnMessageClickListener<MESSAGE>?) {
        this.onMessageClickListener = onMessageClickListener
    }

    /**
     * Sets click listener for message view. Fires ONLY if list is not in selection mode.
     *
     * @param onMessageViewClickListener click listener.
     */
    fun setOnMessageViewClickListener(
        onMessageViewClickListener: OnMessageViewClickListener<MESSAGE>?
    ) {
        this.onMessageViewClickListener =
            onMessageViewClickListener
    }

    /**
     * Registers click listener for view by id
     *
     * @param viewId                     view
     * @param onMessageViewClickListener click listener.
     */
    fun registerViewClickListener(
        viewId: Int,
        onMessageViewClickListener: OnMessageViewClickListener<MESSAGE>
    ) {
        viewClickListenersArray.append(
            viewId,
            onMessageViewClickListener
        )
    }

    /**
     * Sets long click listener for item. Fires only if selection mode is disabled.
     *
     * @param onMessageLongClickListener long click listener.
     */
    fun setOnMessageLongClickListener(
        onMessageLongClickListener: OnMessageLongClickListener<MESSAGE>?
    ) {
        this.onMessageLongClickListener =
            onMessageLongClickListener
    }

    /**
     * Sets long click listener for message view. Fires ONLY if selection mode is disabled.
     *
     * @param onMessageViewLongClickListener long click listener.
     */
    fun setOnMessageViewLongClickListener(
        onMessageViewLongClickListener: OnMessageViewLongClickListener<MESSAGE>?
    ) {
        this.onMessageViewLongClickListener =
            onMessageViewLongClickListener
    }

    /**
     * Set callback to be invoked when list scrolled to top.
     *
     * @param loadMoreListener listener.
     */
    fun setLoadMoreListener(loadMoreListener: OnLoadMoreListener?) {
        this.loadMoreListener = loadMoreListener
    }

    /**
     * Sets custom [DateFormatter.Formatter] for text representation of date headers.
     */
    fun setDateHeadersFormatter(dateHeadersFormatter: DateFormatter.Formatter?) {
        this.dateHeadersFormatter = dateHeadersFormatter
    }

    /*
     * PRIVATE METHODS
     * */
    private fun recountDateHeaders() {
        val indicesToDelete: MutableList<Int> = ArrayList()
        for (i in items!!.indices) {
            val wrapper: Wrapper<*> = items[i]
            if (wrapper.item is Date) {
                if (i == 0) {
                    indicesToDelete.add(i)
                } else {
                    if (items[i - 1].item is Date) {
                        indicesToDelete.add(i)
                    }
                }
            }
        }
        indicesToDelete.reverse()
        for (i in indicesToDelete) {
            items.removeAt(i)
            notifyItemRemoved(i)
        }
    }

    private fun generateHeaders(messages: List<MESSAGE>) {
        for (i in messages.indices) {
            val message = messages[i]

            val messagePos = ListUtils.findSmallItemIndex(this.items, message!!)
            if (messagePos != -1 && messagePos != 0) { // must not be last element
                this.items.add(messagePos, Wrapper(message))
            } else { // normal flow

                items.add(Wrapper(message))

                // Generate total unread
                if (message.isUnread && !CollectionUtils.hasUnreadMessages(items)) {
                    items.add(Wrapper(MessageUnread(CollectionUtils.countUnreadMessages(items))))
                }
                if (messages.size > i + 1) {
                    // add unread message item
                    val nextMessage = messages[i + 1]
                    if (!DateFormatter.isSameDay(message.createdAt, nextMessage!!.createdAt)) {
                        items.add(Wrapper(message.createdAt))
                    }
                } else items.add(Wrapper(message.createdAt))
            }
        }
    }

    private fun checkNextItemDatesAreSame(messagePosition: Int, message: MESSAGE & Any) {
        var nextPosition = messagePosition + 1
        do {
            val nextMessageItem = items.getOrNull(nextPosition)?.item
            if (nextPosition < (items.size - 1) && nextMessageItem is IMessage) {
                if (DateFormatter.isBeforeDay(message.createdAt, nextMessageItem.createdAt)) {
                    items.add(Wrapper(message.createdAt))
                    return
                }
            }
            nextPosition++
        } while (nextMessageItem != null)
    }

    private fun getMessagePositionById(id: String): Int {
        for (i in items!!.indices) {
            val wrapper: Wrapper<*> = items[i]
            if (wrapper.item is IMessage) {
                val message = wrapper.item as MESSAGE
                if (message!!.id.contentEquals(id)) {
                    return i
                }
            }
        }
        return -1
    }

    private fun isPreviousSameDate(
        position: Int,
        dateToCompare: Date
    ): Boolean {
        if (items!!.size <= position) return false
        return if (items[position].item is IMessage) {
            val previousPositionDate =
                (items[position].item as MESSAGE?)!!.createdAt
            DateFormatter.isSameDay(
                dateToCompare,
                previousPositionDate
            )
        } else false
    }

    private fun isPreviousSameAuthor(
        id: String,
        position: Int
    ): Boolean {
        val prevPosition = position + 1
        return if (items!!.size <= prevPosition) false else (items[prevPosition].item is IMessage
            && (items[prevPosition].item as MESSAGE?)!!.user.id.contentEquals(
            id
        ))
    }

    private fun incrementSelectedItemsCount() {
        selectedItemsCount++
        notifySelectionChanged()
    }

    private fun decrementSelectedItemsCount() {
        selectedItemsCount--
        isSelectionModeEnabled = selectedItemsCount > 0
        notifySelectionChanged()
    }

    private fun notifySelectionChanged() {
        if (selectionListener != null) {
            selectionListener!!.onSelectionChanged(
                selectedItemsCount
            )
        }
    }

    private fun notifyMessageClicked(message: MESSAGE) {
        if (onMessageClickListener != null) {
            onMessageClickListener!!.onMessageClick(message)
        }
    }

    private fun notifyMessageViewClicked(
        view: View,
        message: MESSAGE
    ) {
        if (onMessageViewClickListener != null) {
            onMessageViewClickListener!!.onMessageViewClick(
                view,
                message
            )
        }
    }

    private fun notifyMessageLongClicked(message: MESSAGE) {
        if (onMessageLongClickListener != null) {
            onMessageLongClickListener!!.onMessageLongClick(
                message
            )
        }
    }

    private fun notifyMessageViewLongClicked(
        view: View,
        message: MESSAGE
    ) {
        if (onMessageViewLongClickListener != null) {
            onMessageViewLongClickListener!!.onMessageViewLongClick(
                view,
                message
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getMessageClickListener(wrapper: Wrapper<*>): View.OnClickListener {
        return View.OnClickListener { view: View ->
            val messageItem = (wrapper.item as MESSAGE)!!
            if (selectionListener != null && isSelectionModeEnabled) {
                wrapper.isSelected = !wrapper.isSelected
                if (wrapper.isSelected) incrementSelectedItemsCount() else decrementSelectedItemsCount()
                notifyItemChanged(
                    getMessagePositionById(messageItem.id)
                )
            } else {
                notifyMessageClicked(messageItem)
                notifyMessageViewClicked(view, messageItem)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getMessageLongClickListener(wrapper: Wrapper<*>): OnLongClickListener {
        return OnLongClickListener { view: View ->
            val messageItem = (wrapper.item as MESSAGE)!!
            if (selectionListener == null) {
                notifyMessageLongClicked(messageItem)
                notifyMessageViewLongClicked(
                    view,
                    messageItem
                )
            } else {
                isSelectionModeEnabled = true
                view.performClick()
            }
            true
        }
    }

    private fun getSelectedText(
        formatter: Formatter<MESSAGE?>?,
        reverse: Boolean
    ): String {
        val builder = StringBuilder()
        val selectedMessages = selectedMessages
        if (reverse) Collections.reverse(selectedMessages)
        for (message in selectedMessages) {
            builder.append(
                if (formatter == null) message.toString() else formatter.format(
                    message
                )
            )
            builder.append("\n\n")
        }
        builder.replace(
            builder.length - 2,
            builder.length,
            ""
        )
        return builder.toString()
    }

    private fun copyToClipboard(
        context: Context,
        copiedText: String
    ) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip =
            ClipData.newPlainText(copiedText, copiedText)
        clipboard.setPrimaryClip(clip)
    }

    fun setLayoutManager(layoutManager: RecyclerView.LayoutManager?) {
        this.layoutManager = layoutManager
    }

    fun setStyle(style: MessagesListStyle?) {
        messagesListStyle = style
    }

    /*
     * WRAPPER
     * */
    class Wrapper<DATA> internal constructor(var item: DATA) {
        var isSelected = false
    }
    /*
     * LISTENERS
     * */
    /**
     * Interface definition for a callback to be invoked when next part of messages need to be loaded.
     */
    interface OnLoadMoreListener {
        /**
         * Fires when user scrolled to the end of list.
         *
         * @param page            next page to download.
         * @param totalItemsCount current items count.
         */
        fun onLoadMore(page: Int, totalItemsCount: Int)
    }

    /**
     * Interface definition for a callback to be invoked when selected messages count is changed.
     */
    interface SelectionListener {
        /**
         * Fires when selected items count is changed.
         *
         * @param count count of selected items.
         */
        fun onSelectionChanged(count: Int)
    }

    /**
     * Interface definition for a callback to be invoked when message item is clicked.
     */
    interface OnMessageClickListener<MESSAGE : IMessage?> {
        /**
         * Fires when message is clicked.
         *
         * @param message clicked message.
         */
        fun onMessageClick(message: MESSAGE)
    }

    /**
     * Interface definition for a callback to be invoked when message view is clicked.
     */
    interface OnMessageViewClickListener<MESSAGE : IMessage?> {
        /**
         * Fires when message view is clicked.
         *
         * @param message clicked message.
         */
        fun onMessageViewClick(
            view: View?,
            message: MESSAGE
        )
    }

    /**
     * Interface definition for a callback to be invoked when message item is long clicked.
     */
    interface OnMessageLongClickListener<MESSAGE : IMessage?> {
        /**
         * Fires when message is long clicked.
         *
         * @param message clicked message.
         */
        fun onMessageLongClick(message: MESSAGE)
    }

    /**
     * Interface definition for a callback to be invoked when message view is long clicked.
     */
    interface OnMessageViewLongClickListener<MESSAGE : IMessage?> {
        /**
         * Fires when message view is long clicked.
         *
         * @param message clicked message.
         */
        fun onMessageViewLongClick(
            view: View?,
            message: MESSAGE
        )
    }

    /**
     * Interface used to format your message model when copying.
     */
    interface Formatter<MESSAGE> {
        /**
         * Formats an string representation of the message object.
         *
         * @param message The object that should be formatted.
         * @return Formatted text.
         */
        fun format(message: MESSAGE): String?
    }

    /**
     * This class is deprecated. Use [MessageHolders] instead.
     */
    @Deprecated("")
    class HoldersConfig : MessageHolders() {
        /**
         * This method is deprecated. Use [MessageHolders.setIncomingTextConfig] instead.
         *
         * @param holder holder class.
         * @param layout layout resource.
         */
        @Deprecated("")
        fun setIncoming(
            holder: Class<out BaseMessageViewHolder<out IMessage?>?>?,
            @LayoutRes layout: Int
        ) {
            super.setIncomingTextConfig(holder!!, layout)
        }

        /**
         * This method is deprecated. Use [MessageHolders.setIncomingTextHolder] instead.
         *
         * @param holder holder class.
         */
        @Deprecated("")
        fun setIncomingHolder(holder: Class<out BaseMessageViewHolder<out IMessage?>?>?) {
            super.setIncomingTextHolder(holder!!)
        }

        /**
         * This method is deprecated. Use [MessageHolders.setIncomingTextLayout] instead.
         *
         * @param layout layout resource.
         */
        @Deprecated("")
        fun setIncomingLayout(@LayoutRes layout: Int) {
            super.setIncomingTextLayout(layout)
        }

        /**
         * This method is deprecated. Use [MessageHolders.setOutcomingTextConfig] instead.
         *
         * @param holder holder class.
         * @param layout layout resource.
         */
        @Deprecated("")
        fun setOutcoming(
            holder: Class<out BaseMessageViewHolder<out IMessage?>?>?,
            @LayoutRes layout: Int
        ) {
            super.setOutcomingTextConfig(holder!!, layout)
        }

        /**
         * This method is deprecated. Use [MessageHolders.setOutcomingTextHolder] instead.
         *
         * @param holder holder class.
         */
        @Deprecated("")
        fun setOutcomingHolder(holder: Class<out BaseMessageViewHolder<out IMessage?>?>?) {
            super.setOutcomingTextHolder(holder!!)
        }

        /**
         * This method is deprecated. Use [MessageHolders.setOutcomingTextLayout] instead.
         *
         * @param layout layout resource.
         */
        @Deprecated("")
        fun setOutcomingLayout(@LayoutRes layout: Int) {
            this.setOutcomingTextLayout(layout)
        }

        /**
         * This method is deprecated. Use [MessageHolders.setDateHeaderConfig] instead.
         *
         * @param holder holder class.
         * @param layout layout resource.
         */
        @Deprecated("")
        fun setDateHeader(
            holder: Class<out ViewHolder<Date?>?>?,
            @LayoutRes layout: Int
        ) {
            super.setDateHeaderConfig(holder!!, layout)
        }
    }

    /**
     * This class is deprecated. Use [MessageHolders.BaseMessageViewHolder] instead.
     */
    @Deprecated("")
    abstract class BaseMessageViewHolder<MESSAGE : IMessage?>(
        itemView: View?
    ) : MessageHolders.BaseMessageViewHolder<MESSAGE>(
        itemView
    ) {
        private val isSelected = false

        /**
         * Callback for implementing images loading in message list
         */
        protected var imageLoader: ImageLoader? = null

        /**
         * Returns whether is item selected
         *
         * @return weather is item selected.
         */
        override fun isSelected(): Boolean {
            return isSelected
        }

        /**
         * Returns weather is selection mode enabled
         *
         * @return weather is selection mode enabled.
         */
        override fun isSelectionModeEnabled(): Boolean {
            return Companion.isSelectionModeEnabled
        }

        override fun configureLinksBehavior(text: TextView) {
            text.linksClickable = false
            text.movementMethod =
                object : LinkMovementMethod() {
                    override fun onTouchEvent(
                        widget: TextView,
                        buffer: Spannable,
                        event: MotionEvent
                    ): Boolean {
                        var result = false
                        if (!Companion.isSelectionModeEnabled) {
                            result = super.onTouchEvent(
                                widget,
                                buffer,
                                event
                            )
                        }
                        itemView.onTouchEvent(event)
                        return result
                    }
                }
        }
    }

    /**
     * This class is deprecated. Use [MessageHolders.DefaultDateHeaderViewHolder] instead.
     */
    @Deprecated("")
    class DefaultDateHeaderViewHolder(itemView: View) :
        ViewHolder<Date?>(itemView),
        DefaultMessageViewHolder {
        protected var text: TextView?
        protected var dateFormat: String? = null
        protected var dateHeadersFormatter: DateFormatter.Formatter? =
            null

        init {
            text = itemView.findViewById(R.id.messageText)
        }

        override fun onBind(date: Date?) {
            if (text != null) {
                var formattedDate: String? = null
                if (dateHeadersFormatter != null) formattedDate =
                    dateHeadersFormatter!!.format(date)
                text!!.text =
                    formattedDate
                        ?: DateFormatter.format(
                            date,
                            dateFormat
                        )
            }
        }

        @SuppressLint("WrongConstant")
        override fun applyStyle(style: MessagesListStyle) {
            if (text != null) {
                text!!.setTextColor(style.dateHeaderTextColor)
                text!!.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    style.dateHeaderTextSize.toFloat()
                )
                text!!.setTypeface(
                    text!!.typeface,
                    style.dateHeaderTextStyle
                )
                text!!.setPadding(
                    style.dateHeaderPadding,
                    style.dateHeaderPadding,
                    style.dateHeaderPadding,
                    style.dateHeaderPadding
                )
            }
            dateFormat = style.dateHeaderFormat
            dateFormat =
                if (dateFormat == null) DateFormatter.Template.STRING_DAY_MONTH_YEAR.get() else dateFormat
        }
    }

    /**
     * This class is deprecated. Use [MessageHolders.IncomingTextMessageViewHolder] instead.
     */
    @Deprecated("")
    class IncomingMessageViewHolder<MESSAGE : IMessage?>(
        itemView: View?
    ) : IncomingTextMessageViewHolder<MESSAGE>(itemView),
        DefaultMessageViewHolder

    /**
     * This class is deprecated. Use [MessageHolders.OutcomingTextMessageViewHolder] instead.
     */
    @Deprecated("")
    class OutcomingMessageViewHolder<MESSAGE : IMessage?>(
        itemView: View?
    ) : OutcomingTextMessageViewHolder<MESSAGE>(itemView)

    companion object {
        @JvmField
        var isSelectionModeEnabled = false
    }
}
