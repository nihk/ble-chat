package nick.chat.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ble.usability.BluetoothUsability
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nick.chat.chatlist.ServerRepository

class ConversationViewModel(
    private val conversation: ByteArray,
    private val bluetoothUsability: BluetoothUsability,
    private val conversationRepository: ConversationRepository,
    private val serverRepository: ServerRepository
) : ViewModel() {
    private val useBluetoothRequests = MutableSharedFlow<Unit>()

    val items = useBluetoothRequests.flatMapLatest { conversationRepository.items(conversation) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT.inWholeMilliseconds),
            initialValue = null
        )
        .filterNotNull()

    val serverEvents = useBluetoothRequests.flatMapLatest { serverRepository.events() }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT.inWholeMilliseconds)
        )

    val sideEffects = bluetoothUsability.sideEffects()
        .onEach { sideEffect ->
            if (sideEffect == BluetoothUsability.SideEffect.UseBluetooth) {
                useBluetoothRequests.emit(Unit)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT.inWholeMilliseconds),
            initialValue = null
        )
        .filterNotNull()

    fun tryUsingBluetooth() {
        viewModelScope.launch { bluetoothUsability.checkUsability() }
    }

    companion object {
        private val TIMEOUT = 5.toDuration(DurationUnit.SECONDS)
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
