package nick.chat.conversation

import ble.connecting.BluetoothConnector
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import nick.chat.bluetooth.CharacteristicParser
import nick.chat.data.CurrentTime
import nick.chat.data.local.Message
import nick.chat.data.local.MessagesDao

interface ConversationRepository {
    fun items(conversation: ByteArray): Flow<List<ConversationItem>>
    fun connectTo(conversation: ByteArray, address: String): Flow<BluetoothConnector.State>
    fun send(data: String)
}

class BluetoothConversationRepository @Inject constructor(
    private val bluetoothConnector: BluetoothConnector,
    private val characteristicParser: CharacteristicParser,
    private val messagesDao: MessagesDao,
    private val currentTime: CurrentTime
) : ConversationRepository {

    override fun items(conversation: ByteArray): Flow<List<ConversationItem>> {
        return messagesDao.selectAllByConversation(conversation).map { messages ->
            messages.toConversationItems()
        }
    }

    override fun connectTo(conversation: ByteArray, address: String): Flow<BluetoothConnector.State> {
        return bluetoothConnector.connect(address)
            .onEach { state ->
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
