package nick.chat.conversation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import nick.chat.data.Resource
import ble.connecting.BluetoothConnector
import nick.chat.data.local.Message
import javax.inject.Inject

interface ConversationRepository {
    fun messages(): Flow<Resource<List<Message>>>
}

class BluetoothConversationRepository @Inject constructor(
    private val bluetoothConnector: BluetoothConnector
) : ConversationRepository {

    override fun messages(): Flow<Resource<List<Message>>> {
        return emptyFlow()
    }
}