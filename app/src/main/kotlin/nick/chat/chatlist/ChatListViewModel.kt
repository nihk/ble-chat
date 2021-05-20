package nick.chat.chatlist

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
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

class ChatListViewModel(
    private val bluetoothUsability: BluetoothUsability,
    private val chatListRepository: ChatListRepository,
    private val serverRepository: ServerRepository
) : ViewModel() {
    private val useBluetoothRequests = MutableSharedFlow<Unit>()

    val items = useBluetoothRequests.flatMapLatest { chatListRepository.items() }
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
        private val chatListRepository: ChatListRepository,
        private val serverRepository: ServerRepository
    ) {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel?> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return ChatListViewModel(
                        bluetoothUsability,
                        chatListRepository,
                        serverRepository
                    ) as T
                }
            }
        }
    }
}
