package nick.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ble.usability.BluetoothUsability
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nick.chat.chatlist.ServerRepository

class MainViewModel(
    private val bluetoothUsability: BluetoothUsability,
    private val serverRepository: ServerRepository
) : ViewModel() {
    private val snackbars = MutableSharedFlow<SnackbarRetryBluetooth>()
    fun snackbars(): Flow<SnackbarRetryBluetooth> = snackbars
    private val useBluetooth = MutableStateFlow(false)
    fun useBluetooth(): Flow<Boolean> = useBluetooth

    val sideEffects = bluetoothUsability.sideEffects()
        .onEach { sideEffect ->
            if (sideEffect == BluetoothUsability.SideEffect.UseBluetooth) {
                useBluetooth.emit(true)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT.inWholeMilliseconds),
            initialValue = null
        )
        .filterNotNull()

    val serverEvents = useBluetooth.filter { it }
        .flatMapLatest { serverRepository.events() }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT.inWholeMilliseconds)
        )

    fun tryUsingBluetooth() {
        viewModelScope.launch { bluetoothUsability.checkUsability() }
    }

    fun promptRetryBluetooth(
        message: String,
        buttonText: String
    ) {
        viewModelScope.launch {
            snackbars.emit(
                SnackbarRetryBluetooth(
                    message = message,
                    buttonText = buttonText
                )
            )
        }
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

    data class SnackbarRetryBluetooth(
        val message: String,
        val buttonText: String
    )
}
