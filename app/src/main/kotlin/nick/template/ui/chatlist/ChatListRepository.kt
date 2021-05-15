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
import nick.template.data.local.ChatListItemDao
import nick.template.data.local.DeviceDao
import nick.template.data.local.Message
import nick.template.data.local.MessageDao

interface ChatListRepository {
    fun items(): Flow<Resource<List<ChatListItem>>>
    suspend fun insertMessage(message: Message)
}

class BluetoothChatListRepository @Inject constructor(
    private val bluetoothScanner: OneShotBluetoothScanner,
    private val chatListItemDao: ChatListItemDao,
    private val deviceDao: DeviceDao,
    private val messageDao: MessageDao,
    private val deviceCacheThreshold: DeviceCacheThreshold
) : ChatListRepository {

    override fun items(): Flow<Resource<List<ChatListItem>>> = flow {
        emit(Resource.Loading())
        emit(Resource.Loading(chatListItemDao.selectAll().first()))

        val flow = when (val result = bluetoothScanner.result()) {
            is BluetoothScanner.Result.Error -> chatListItemDao.selectAll().map { items -> Resource.Error(items, result.error)}
            is BluetoothScanner.Result.Success -> {
                deviceDao.insertAndPurgeOldDevices(result.devices, deviceCacheThreshold.threshold)
                chatListItemDao.selectAll().map { items -> Resource.Success(items) }
            }
        }

        emitAll(flow)
    }

    override suspend fun insertMessage(message: Message) {
        messageDao.insert(message)
    }
}
