package nick.template.ui.devices

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
import nick.template.data.Resource
import nick.template.data.bluetooth.ScanningRepository
import nick.template.data.bluetooth.BluetoothUsability
import nick.template.data.local.Device
import javax.inject.Inject

class DevicesViewModel(
    private val bluetoothUsability: BluetoothUsability,
    private val repository: ScanningRepository
) : ViewModel() {
    private var scanning: Job? = null

    private val devices = MutableStateFlow<Resource<List<Device>>?>(null)
    fun devices(): Flow<Resource<List<Device>>> = devices.filterNotNull()

    fun bluetoothUsability(): Flow<BluetoothUsability.SideEffect> = bluetoothUsability.sideEffects()

    fun scanForDevices() {
        scanning?.cancel()
        scanning = repository.devices()
            .onEach { devices.value = it }
            .launchIn(viewModelScope)
    }

    fun promptIfNeeded() {
        viewModelScope.launch { bluetoothUsability.handleEvent(BluetoothUsability.Event.PromptIfNeeded) }
    }

    fun denyPermissions() {
        viewModelScope.launch { bluetoothUsability.handleEvent(BluetoothUsability.Event.DenyPermissions) }
    }

    fun denyTurningBluetoothOn() {
        viewModelScope.launch { bluetoothUsability.handleEvent(BluetoothUsability.Event.DenyTurningBluetoothOn) }
    }

    fun denyTurningLocationOn() {
        viewModelScope.launch { bluetoothUsability.handleEvent(BluetoothUsability.Event.DenyTurningLocationOn) }
    }

    class Factory @Inject constructor(
        private val bluetoothUsability: BluetoothUsability,
        private val repository: ScanningRepository
    ) {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel?> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return DevicesViewModel(bluetoothUsability, repository) as T
                }
            }
        }
    }
}
