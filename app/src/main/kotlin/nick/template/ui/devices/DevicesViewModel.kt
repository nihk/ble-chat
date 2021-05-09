package nick.template.ui.devices

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import nick.template.data.Resource
import nick.template.data.bluetooth.BluetoothUsability
import nick.template.data.bluetooth.ScanningRepository
import nick.template.data.local.Device
import javax.inject.Inject

class DevicesViewModel(
    private val bluetoothUsability: BluetoothUsability,
    private val repository: ScanningRepository
) : ViewModel() {
    private val devices = MutableStateFlow<Resource<List<Device>>?>(null)
    fun devices(): Flow<Resource<List<Device>>> = devices.filterNotNull()

    private val scanningRequests = MutableSharedFlow<Unit>()

    init {
        scanningRequests.flatMapLatest { repository.scan() }
            .onEach { devices.value = it }
            .launchIn(viewModelScope)
    }

    fun bluetoothUsability(): Flow<BluetoothUsability.SideEffect> = bluetoothUsability.sideEffects()
        .onEach { sideEffect ->
            if (sideEffect == BluetoothUsability.SideEffect.UseBluetooth) {
                scanningRequests.emit(Unit)
            }
        }

    fun tryUsingBluetooth() {
        viewModelScope.launch { bluetoothUsability.checkUsability() }
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
