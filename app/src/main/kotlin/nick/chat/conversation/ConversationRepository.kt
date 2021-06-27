package nick.chat.conversation

import ble.connecting.BluetoothConnector
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nick.chat.bluetooth.CharacteristicParser
import nick.chat.data.CurrentTime
import nick.chat.data.local.Message
import nick.chat.data.local.MessagesDao

interface ConversationRepository {
    fun items(conversation: ByteArray, address: String): Flow<ConversationResource>
    fun send(data: String)
}

class BluetoothConversationRepository @Inject constructor(
    private val bluetoothConnector: BluetoothConnector,
    private val characteristicParser: CharacteristicParser,
    private val messagesDao: MessagesDao,
    private val currentTime: CurrentTime
) : ConversationRepository {

    override fun items(conversation: ByteArray, address: String): Flow<ConversationResource> {
        return combine(bluetoothConnector.connect(address), messagesDao.selectAllByConversation(conversation)) { state, messages ->
            when (state) {
                is BluetoothConnector.State.CharacteristicWritten -> {
                    val (_, text) = characteristicParser.parse(state.data)
                    val message = Message(
                        conversation = conversation,
                        isMe = true,
                        text = text,
                        timestamp = currentTime.millis()
                    )
                    messagesDao.insert(message)
                }
            }
            ConversationResource(state, messages.toConversationItems())
        }
    }

    override fun send(data: String) {
        bluetoothConnector.send(data)
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
