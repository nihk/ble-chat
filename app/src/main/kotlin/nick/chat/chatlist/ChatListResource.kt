package nick.chat.chatlist

sealed class ChatListResource {
    abstract val items: List<ChatListItem>?
    data class Loading(override val items: List<ChatListItem>? = null) : ChatListResource()
    data class Success(override val items: List<ChatListItem>? = null) : ChatListResource()
    data class Error(override val items: List<ChatListItem>? = null, val throwable: Throwable): ChatListResource()
}
