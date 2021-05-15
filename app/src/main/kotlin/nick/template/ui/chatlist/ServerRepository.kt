package nick.template.ui.chatlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import nick.template.data.CurrentTime
import nick.template.data.bluetooth.BluetoothError
import nick.template.data.bluetooth.advertising.BluetoothAdvertiser
import nick.template.data.bluetooth.serving.BluetoothServer
import nick.template.data.local.Message
import nick.template.data.local.MessageDao

interface ServerRepository {
    fun events(): Flow<Event>

    sealed class Event {
        data class AdvertisingFailed(val error: BluetoothError) : Event()
        object Disconnected : Event()
    }
}

class BroadcastingServerRepository @Inject constructor(
    private val advertiser: BluetoothAdvertiser,
    private val server: BluetoothServer,
    private val messageDao: MessageDao,
    private val currentTime: CurrentTime
) : ServerRepository {

    override fun events(): Flow<ServerRepository.Event> {
        val serverEvents = server.events()
            .onEach { event ->
                if (event is BluetoothServer.Event.Message) {
                    val message = Message(
                        conversation = event.identifier,
                        isMe = false,
                        text = event.message,
                        timestamp = currentTime.millis()
                    )
                    messageDao.insert(message)
                }
            }

        // fixme: serverEvents are delayed until a device disconnects, so this won't emit anything til both flows emit once
        return combine(advertiser.start(), serverEvents) { startResult, serverEvent ->
            Pair(startResult, serverEvent)
        }.transform {  pair ->
            val startResult = pair.first
            val serverEvent = pair.second
            // fixme: if start result has an error, only it will be emitted here
            when {
                startResult is BluetoothAdvertiser.StartResult.Error ->
                    emit(ServerRepository.Event.AdvertisingFailed(startResult.error))
                serverEvent is BluetoothServer.Event.Disconnected ->
                    emit(ServerRepository.Event.Disconnected)
            }
        }
    }
}
