package nick.chat.chatlist

import androidx.navigation.NavController
import javax.inject.Inject
import nick.chat.navigation.AppNavGraph
import nick.chat.conversation.ConversationFragment

interface OpenConversationCallback {
    fun with(item: ChatListItem)
}

class DefaultOpenConversationCallback @Inject constructor(
    private val navController: NavController
) : OpenConversationCallback {
    override fun with(item: ChatListItem) {
        navController.navigate(
            AppNavGraph.Actions.toConversation,
            ConversationFragment.bundle(item.address)
        )
    }
}
