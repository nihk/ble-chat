package nick.template.ui.chatlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import nick.template.data.Resource
import nick.template.data.bluetooth.scanning.BluetoothScanner
import nick.template.data.bluetooth.scanning.DeviceCacheThreshold
import nick.template.data.bluetooth.scanning.OneShotBluetoothScanner
import nick.template.data.local.DeviceAndMessages
import nick.template.data.local.DeviceAndMessagesDao
import nick.template.data.local.Message

interface ChatListRepository {
    fun items(): Flow<Resource<List<ChatListItem>>>
}

class ScanningChatListRepository @Inject constructor(
    private val bluetoothScanner: OneShotBluetoothScanner,
    private val dao: DeviceAndMessagesDao,
    private val deviceCacheThreshold: DeviceCacheThreshold
) : ChatListRepository {

    override fun items(): Flow<Resource<List<ChatListItem>>> = flow {
        emit(Resource.Loading())
        emit(Resource.Loading(dao.selectAll().first().toChatListItems()))

        val flow = when (val result = bluetoothScanner.result()) {
            is BluetoothScanner.Result.Error -> dao.selectAll()
                .map { items -> Resource.Error(items.toChatListItems(), result.error) }
            is BluetoothScanner.Result.Success -> {
                dao.insertAndPurgeOldDevices(result.devices, deviceCacheThreshold.threshold)
                dao.selectAll()
                    .map { items -> Resource.Success(items.toChatListItems()) }
            }
        }

        emitAll(flow)
    }

    private fun List<DeviceAndMessages>.toChatListItems(): List<ChatListItem> {
        return map { deviceAndMessages ->
            ChatListItem(
                messageIdentifier = deviceAndMessages.device.messageIdentifier.toList(),
                address = deviceAndMessages.device.address,
                name = deviceAndMessages.device.name,
                latestMessage = deviceAndMessages.messages.maxByOrNull(Message::timestamp)?.text
            )
        }
    }
}
