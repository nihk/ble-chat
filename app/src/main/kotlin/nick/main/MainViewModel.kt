package nick.main

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

// todo: combine sideEffects and serverEvents into one flow?
class MainViewModel(
    private val bluetoothUsability: BluetoothUsability,
    private val serverRepository: ServerRepository
) : ViewModel() {
    private val startServer = MutableSharedFlow<Unit>(
        // This Flow can receive events before subscribers are present.
        replay = 1
    )

    val sideEffects = bluetoothUsability.sideEffects()
        .onEach { sideEffect ->
            if (sideEffect == BluetoothUsability.SideEffect.UseBluetooth) {
                startServer.emit(Unit)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT.inWholeMilliseconds),
            initialValue = null
        )
        .filterNotNull()

    val serverEvents = startServer.flatMapLatest { serverRepository.events() }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT.inWholeMilliseconds)
        )

    fun tryUsingBluetooth() {
        viewModelScope.launch { bluetoothUsability.checkUsability() }
    }

    fun connectTo(address: String) {

    }

    companion object {
        private val SUBSCRIBE_TIMEOUT = 5.toDuration(DurationUnit.SECONDS)
    }

    class Factory @Inject constructor(
        private val bluetoothUsability: BluetoothUsability,
        private val serverRepository: ServerRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(bluetoothUsability, serverRepository) as T
        }
    }
}
