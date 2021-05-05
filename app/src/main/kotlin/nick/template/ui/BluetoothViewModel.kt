package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import nick.template.data.BluetoothPermissions
import nick.template.data.BluetoothRepository
import nick.template.data.BluetoothScanner
import nick.template.data.BluetoothState
import javax.inject.Inject

class BluetoothViewModel(
    private val repository: BluetoothRepository
) : ViewModel() {

    private val userPromptEvents = MutableSharedFlow<UserPromptEvent>()

    private enum class UserPromptEvent {
        PromptIfNeeded,
        DeniedPermissions,
        DeniedTurningBluetoothOn
    }

    // todo: integrate an SQLite database which stores devices. A timestamp can accompany each
    //  device and each insertion is done in a transaction that purges devices older than some
    //  value, e.g. 30 seconds.

    // todo: consider subscribing to this in viewModelScope, and having Fragment listen to
    //  a StateFlow. the advantage is that there is an easy cache for Fragment to use, and config
    //  changes don't have to restart this flow - only backgrounding + foregrounding the app.
    //  it also *might* be easier to integrate Room usage, here.

    fun states(): Flow<State> {
        return combine(
            userPromptEvents.onStart { emit(UserPromptEvent.PromptIfNeeded) },
            repository.bluetoothStates()
        ) { userPromptState, bluetoothState ->
            Pair(userPromptState, bluetoothState)
        }
            .flatMapLatest { pair ->
                val userPromptState = pair.first
                val bluetoothState = pair.second
                val permissionsState = repository.permissionsState()
                when {
                    permissionsState is BluetoothPermissions.State.MissingPermissions -> {
                        when (userPromptState) {
                            UserPromptEvent.DeniedPermissions -> flowOf(State.DeniedPermissions)
                            else -> flowOf(State.RequestPermissions(permissionsState.permissions))
                        }
                    }
                    bluetoothState !is BluetoothState.On -> {
                        when (userPromptState) {
                            UserPromptEvent.DeniedTurningBluetoothOn -> flowOf(State.DeniedEnablingBluetooth)
                            else -> flowOf(State.AskToTurnBluetoothOn)
                        }
                    }
                    else -> repository.scanningResults()
                        .map { result ->
                            @Suppress("USELESS_CAST")
                            State.Scanned(result) as State
                        }
                        .onStart { emit(State.StartedScanning) }
                }
            }
    }

    fun userDeniedPermissions() {
        emit(UserPromptEvent.DeniedPermissions)
    }

    fun userGrantedPermissions() {
        emit(UserPromptEvent.PromptIfNeeded)
    }

    fun userWantsToGrantPermissions() {
        emit(UserPromptEvent.PromptIfNeeded)
    }

    fun userDeniedTurningBluetoothOn() {
        emit(UserPromptEvent.DeniedTurningBluetoothOn)
    }

    fun userWantsToTurnBluetoothOn() {
        emit(UserPromptEvent.PromptIfNeeded)
    }

    private fun emit(userPromptEvent: UserPromptEvent) {
        viewModelScope.launch {
            userPromptEvents.emit(userPromptEvent)
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

sealed class State {
    data class RequestPermissions(val permissions: List<String>) : State()
    object AskToTurnBluetoothOn : State()
    object DeniedEnablingBluetooth : State()
    object DeniedPermissions : State()
    object StartedScanning : State()
    data class Scanned(val result: BluetoothScanner.Result) : State()
}
