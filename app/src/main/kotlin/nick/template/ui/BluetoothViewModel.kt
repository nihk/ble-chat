package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import nick.template.data.bluetooth.BluetoothRepository
import nick.template.data.bluetooth.BluetoothUsability
import nick.template.data.bluetooth.DevicesResource
import javax.inject.Inject

class BluetoothViewModel(
    private val bluetoothUsability: BluetoothUsability,
    private val repository: BluetoothRepository
) : ViewModel() {
    private var scanning: Job? = null

    private val devices = MutableStateFlow<DevicesResource?>(null)
    fun devices(): Flow<DevicesResource> = devices.filterNotNull()

    fun bluetoothUsability(): Flow<BluetoothUsability.Event> = bluetoothUsability.events()

    fun scanForDevices() {
        scanning?.cancel()
        scanning = repository.devices()
            .onEach { devices.value = it }
            .launchIn(viewModelScope)
    }

    fun promptIfNeeded() {
        viewModelScope.launch { bluetoothUsability.promptIfNeeded() }
    }

    fun denyPermissions() {
        viewModelScope.launch { bluetoothUsability.denyPermissions() }
    }

    fun denyTurningBluetoothOn() {
        viewModelScope.launch { bluetoothUsability.denyTurningBluetoothOn() }
    }

    class Factory @Inject constructor(
        private val bluetoothUsability: BluetoothUsability,
        private val repository: BluetoothRepository
    ) {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel?> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return BluetoothViewModel(bluetoothUsability, repository) as T
                }
            }
        }
    }
}
