package nick.chat.chatlist

import ble.scanning.BluetoothScanner
import ble.scanning.OneShotBluetoothScanner
import ble.scanning.Scan
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import nick.chat.data.CurrentTime
import nick.chat.data.local.DeviceCacheThreshold
import nick.chat.data.local.Device
import nick.chat.data.local.DeviceAndMessages
import nick.chat.data.local.DeviceAndMessagesDao
import nick.chat.data.local.DevicesDao
import nick.chat.data.local.Message

interface ChatListRepository {
    fun items(): Flow<ChatListResource>
}

class ScanningChatListRepository @Inject constructor(
    private val bluetoothScanner: OneShotBluetoothScanner,
    private val deviceAndMessagesDao: DeviceAndMessagesDao,
    private val devicesDao: DevicesDao,
    private val deviceCacheThreshold: DeviceCacheThreshold,
    private val currentTime: CurrentTime
) : ChatListRepository {

    override fun items(): Flow<ChatListResource> = flow {
        emit(ChatListResource.Loading())
        emit(ChatListResource.Loading(deviceAndMessagesDao.selectAll().first().toChatListItems()))

        val flow = when (val result = bluetoothScanner.result()) {
            is BluetoothScanner.Result.Error -> deviceAndMessagesDao.selectAll()
                .map { items -> ChatListResource.Error(items.toChatListItems(), result.error) }
            is BluetoothScanner.Result.Success -> {
                devicesDao.insertAndPurgeOldDevices(
                    result.scans.toDevices(),
                    deviceCacheThreshold.threshold
                )
                deviceAndMessagesDao.selectAll()
                    .map { items -> ChatListResource.Success(items.toChatListItems()) }
            }
        }

        emitAll(flow)
    }

    private fun List<DeviceAndMessages>.toChatListItems(): List<ChatListItem> {
        return map { deviceAndMessages ->
            ChatListItem(
                identifier = deviceAndMessages.device.identifier.toList(),
                address = deviceAndMessages.device.address,
                name = deviceAndMessages.device.name,
                latestMessage = deviceAndMessages.messages.maxByOrNull(Message::timestamp)?.text
            )
        }
    }

    private fun List<Scan>.toDevices(): List<Device> {
        return map { scan ->
            Device(
                // todo: can this be more safely retrieved?
                identifier = scan.services.values.first()!!,
                address = scan.address,
                name = scan.name,
                lastSeen = currentTime.millis()
            )
        }
    }
}
