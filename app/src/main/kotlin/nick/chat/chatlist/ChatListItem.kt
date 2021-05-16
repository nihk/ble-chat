package nick.chat.chatlist

data class ChatListItem(
    val messageIdentifier: List<Byte>,
    val address: String,
    val name: String?,
    val latestMessage: String?
)
