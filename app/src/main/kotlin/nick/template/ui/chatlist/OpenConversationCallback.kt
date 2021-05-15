package nick.template.ui.chatlist

import androidx.navigation.NavController
import javax.inject.Inject
import nick.template.navigation.AppNavGraph
import nick.template.ui.conversation.ConversationFragment

interface OpenConversationCallback {
    fun with(chatListItem: ChatListItem)
}

class DefaultOpenConversationCallback @Inject constructor(
    private val navController: NavController
) : OpenConversationCallback {
    override fun with(chatListItem: ChatListItem) {
        navController.navigate(
            AppNavGraph.Actions.toConversation,
            ConversationFragment.bundle(chatListItem.device.address)
        )
    }
}
