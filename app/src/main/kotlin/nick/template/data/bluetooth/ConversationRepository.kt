package nick.template.data.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import nick.template.data.Resource
import nick.template.data.local.Message
import nick.template.data.local.MessageDao
import javax.inject.Inject

interface ConversationRepository {
    fun messages(): Flow<Resource<List<Message>>>
}

class BluetoothConversationRepository @Inject constructor(
    private val bluetoothConnector: BluetoothConnector,
    private val messageDao: MessageDao
) : ConversationRepository {

    override fun messages(): Flow<Resource<List<Message>>> {
        return emptyFlow()
    }
}
