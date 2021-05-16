package nick.chat.chatlist

data class ChatListItem(
    val identifier: List<Byte>,
    val address: String,
    val name: String?,
    val latestMessage: String?
)
