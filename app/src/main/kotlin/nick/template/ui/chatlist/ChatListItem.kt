package nick.template.ui.chatlist

data class ChatListItem(
    val messageIdentifier: List<Byte>,
    val address: String,
    val name: String?,
    val latestMessage: String?
)
