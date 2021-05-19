package nick.chat.conversation

data class ConversationItem(
    val id: Long,
    val isMe: Boolean,
    val message: String
)
