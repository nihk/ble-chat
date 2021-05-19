package nick.chat.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ble.usability.BluetoothUsability
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import nick.chat.chatlist.ServerRepository
import nick.chat.data.Resource

class ConversationViewModel(
    private val conversation: ByteArray,
    private val bluetoothUsability: BluetoothUsability,
    private val conversationRepository: ConversationRepository,
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val items = MutableStateFlow<Resource<List<ConversationItem>>?>(null)
    fun items(): Flow<Resource<List<ConversationItem>>> = items.filterNotNull()

    private val startServerRequests = MutableSharedFlow<Unit>()
    fun serverEvents(): Flow<ServerRepository.Event> = startServerRequests.flatMapLatest {
        serverRepository.events()
    }

    private val connectRequests = MutableSharedFlow<Unit>()
    private val useBluetoothRequests = MutableSharedFlow<Unit>()

    init {
        useBluetoothRequests
            .onEach { connectRequests.emit(Unit) }
            .flatMapLatest { conversationRepository.items(conversation) }
            .onEach { items.value = it }
            .launchIn(viewModelScope)
    }

    fun bluetoothUsability(): Flow<BluetoothUsability.SideEffect> = bluetoothUsability.sideEffects()
        .onEach { sideEffect ->
            if (sideEffect == BluetoothUsability.SideEffect.UseBluetooth) {
                useBluetoothRequests.emit(Unit)
            }
        }

    fun tryUsingBluetooth() {
        viewModelScope.launch { bluetoothUsability.checkUsability() }
    }

    class Factory @Inject constructor(
        private val bluetoothUsability: BluetoothUsability,
        private val conversationRepository: ConversationRepository,
        private val serverRepository: ServerRepository
    ) {
        fun create(conversation: ByteArray): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ConversationViewModel(
                        conversation,
                        bluetoothUsability,
                        conversationRepository,
                        serverRepository
                    ) as T
                }
            }
        }
    }
}
