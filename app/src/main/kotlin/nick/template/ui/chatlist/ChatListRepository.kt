package nick.template.ui.chatlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import nick.template.data.CurrentTime
import nick.template.data.Resource
import nick.template.data.bluetooth.scanning.BluetoothScanner
import nick.template.data.bluetooth.scanning.DeviceCacheThreshold
import nick.template.data.bluetooth.scanning.OneShotBluetoothScanner
import nick.template.data.local.DeviceAndMessages
import nick.template.data.local.DeviceAndMessagesDao
import nick.template.data.local.DeviceDao
import nick.template.data.local.Message
import nick.template.data.local.MessageDao

interface ChatListRepository {
    fun items(): Flow<Resource<List<ChatListItem>>>
    suspend fun insertMessage(address: String, text: String)
}

class ScanningChatListRepository @Inject constructor(
    private val bluetoothScanner: OneShotBluetoothScanner,
    private val deviceAndMessagesDao: DeviceAndMessagesDao,
    private val deviceDao: DeviceDao,
    private val messageDao: MessageDao,
    private val deviceCacheThreshold: DeviceCacheThreshold,
    private val currentTime: CurrentTime
) : ChatListRepository {

    override fun items(): Flow<Resource<List<ChatListItem>>> = flow {
        emit(Resource.Loading())
        emit(Resource.Loading(deviceAndMessagesDao.selectAll().first().toChatListItems()))

        val flow = when (val result = bluetoothScanner.result()) {
            is BluetoothScanner.Result.Error -> deviceAndMessagesDao.selectAll()
                .map { items -> Resource.Error(items.toChatListItems(), result.error) }
            is BluetoothScanner.Result.Success -> {
                deviceDao.insertAndPurgeOldDevices(result.devices, deviceCacheThreshold.threshold)
                deviceAndMessagesDao.selectAll()
                    .map { items -> Resource.Success(items.toChatListItems()) }
            }
        }

        emitAll(flow)
    }

    private fun List<DeviceAndMessages>.toChatListItems(): List<ChatListItem> {
        return map { deviceAndMessages ->
            ChatListItem(
                address = deviceAndMessages.device.address,
                name = deviceAndMessages.device.name,
                latestMessage = deviceAndMessages.messages.maxByOrNull(Message::timestamp)?.text
            )
        }
    }

    override suspend fun insertMessage(address: String, text: String) {
        val message = Message(
            conversation = address,
            isMe = false,
            text = text,
            timestamp = currentTime.millis()
        )
        messageDao.insert(message)
    }
}
