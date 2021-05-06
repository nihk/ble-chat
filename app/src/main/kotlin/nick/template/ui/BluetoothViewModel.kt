package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import nick.template.data.bluetooth.BluetoothPermissions
import nick.template.data.bluetooth.BluetoothRepository
import nick.template.data.bluetooth.BluetoothState
import nick.template.data.bluetooth.DevicesResource
import javax.inject.Inject

class BluetoothViewModel(
    private val repository: BluetoothRepository
) : ViewModel() {

    private val actions = MutableSharedFlow<Action>()

    private enum class Action {
        PromptIfNeeded,
        DenyPermissions,
        DenyTurningOnBluetooth
    }

    private var scanning: Job? = null

    private val devices = MutableStateFlow<DevicesResource?>(null)
    fun devices(): Flow<DevicesResource> = devices.filterNotNull()

    // todo: move this to its own, reusable class (so chat fragment can also use)
    fun events(): Flow<Event> {
        return combine(
            actions.onStart { emit(Action.PromptIfNeeded) },
            repository.bluetoothStates()
        ) { userPromptState, bluetoothState ->
            Pair(userPromptState, bluetoothState)
        }
            .map { pair ->
                val userAction = pair.first
                val bluetoothState = pair.second
                val permissionsState = repository.permissionsState()
                when {
                    permissionsState is BluetoothPermissions.State.MissingPermissions -> {
                        when (userAction) {
                            Action.DenyPermissions -> Event.DeniedPermissions
                            else -> Event.RequestPermissions(permissionsState.permissions)
                        }
                    }
                    bluetoothState !is BluetoothState.On -> {
                        when (userAction) {
                            Action.DenyTurningOnBluetooth -> Event.DeniedTurningOnBluetooth
                            else -> Event.AskToTurnBluetoothOn
                        }
                    }
                    else -> Event.CanUseBluetooth
                }
            }
    }

    fun scanForDevices() {
        scanning?.cancel()
        scanning = repository.devices()
            .onEach { devices.value = it }
            .launchIn(viewModelScope)
    }

    fun promptIfNeeded() {
        emit(Action.PromptIfNeeded)
    }

    fun denyPermissions() {
        emit(Action.DenyPermissions)
    }

    fun denyTurningBluetoothOn() {
        emit(Action.DenyTurningOnBluetooth)
    }

    private fun emit(action: Action) {
        viewModelScope.launch {
            actions.emit(action)
        }
    }

    class Factory @Inject constructor(
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
                    return BluetoothViewModel(repository) as T
                }
            }
        }
    }
}

sealed class Event {
    object CanUseBluetooth : Event()
    data class RequestPermissions(val permissions: List<String>) : Event()
    object DeniedPermissions : Event()
    object AskToTurnBluetoothOn : Event()
    object DeniedTurningOnBluetooth : Event()
}
