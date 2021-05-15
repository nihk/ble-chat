package nick.template.ui.chatlist

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import nick.template.data.Resource
import nick.template.data.bluetooth.advertising.AdvertisingRepository
import nick.template.data.bluetooth.advertising.BluetoothAdvertiser
import nick.template.data.bluetooth.usability.BluetoothUsability

class ChatListViewModel(
    private val bluetoothUsability: BluetoothUsability,
    private val chatListRepository: ChatListRepository,
    private val advertisingRepository: AdvertisingRepository
) : ViewModel() {
    private val items = MutableStateFlow<Resource<List<ChatListItem>>?>(null)
    fun items(): Flow<Resource<List<ChatListItem>>> = items.filterNotNull()

    private val advertisingRequests = MutableSharedFlow<Unit>()
    fun advertising(): Flow<BluetoothAdvertiser.StartResult> = advertisingRequests.flatMapLatest {
        advertisingRepository.advertise()
    }

    private val useBluetoothRequests = MutableSharedFlow<Unit>()

    init {
        useBluetoothRequests
            .onEach { advertisingRequests.emit(Unit) }
            .flatMapLatest { chatListRepository.items() }
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
        private val chatListRepository: ChatListRepository,
        private val advertisingRepository: AdvertisingRepository
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
                        advertisingRepository
                    ) as T
                }
            }
        }
    }
}
