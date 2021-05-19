package nick.chat.conversation

import ble.connecting.BluetoothConnector
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nick.chat.data.Resource
import nick.chat.data.local.Message
import nick.chat.data.local.MessagesDao

interface ConversationRepository {
    fun items(conversation: ByteArray): Flow<Resource<List<ConversationItem>>>
}

class BluetoothConversationRepository @Inject constructor(
    private val bluetoothConnector: BluetoothConnector,
    private val messagesDao: MessagesDao
) : ConversationRepository {

    override fun items(conversation: ByteArray): Flow<Resource<List<ConversationItem>>> {
        return messagesDao.selectAllByConversation(conversation).map { messages ->
            Resource.Success(messages.toConversationItems())
        }
    }
}

private fun List<Message>.toConversationItems(): List<ConversationItem> {
    return map { message ->
        ConversationItem(
            id = message.id,
            isMe = message.isMe,
            message = message.text
        )
    }
}
