package nick.chat.chatlist

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import ble.usability.BluetoothUsability
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatListViewModel(
    chatListRepository: ChatListRepository
) : ViewModel() {
    private var previousSideEffect: BluetoothUsability.SideEffect? = null
    private val refresh = MutableSharedFlow<Unit>(
        // Don't let any flatMapping of `refresh` get missed.
        replay = 1
    )

    val items = refresh.flatMapLatest { chatListRepository.items() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )
        .filterNotNull()

    fun refresh() {
        viewModelScope.launch { refreshInternal() }
    }

    suspend fun handleSideEffect(sideEffect: BluetoothUsability.SideEffect) {
        if (previousSideEffect == sideEffect) return
        previousSideEffect = sideEffect
        if (sideEffect is BluetoothUsability.SideEffect.UseBluetooth) {
            refreshInternal()
        }
    }

    private suspend fun refreshInternal() {
        refresh.emit(Unit)
    }

    class Factory @Inject constructor(
        private val chatListRepository: ChatListRepository
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
                        chatListRepository
                    ) as T
                }
            }
        }
    }
}
