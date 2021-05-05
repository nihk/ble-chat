package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import nick.template.data.BluetoothPermissions
import nick.template.data.BluetoothRepository
import nick.template.data.BluetoothScanner
import nick.template.data.BluetoothState
import javax.inject.Inject

class BluetoothViewModel(
    private val repository: BluetoothRepository
) : ViewModel() {
    private val userPromptStates: MutableStateFlow<UserPromptState> = MutableStateFlow(UserPromptState.Initial)

    enum class UserPromptState {
        Initial,
        // Permissions
        WantsToGrantPermissions,
        DeniedPermissions,
        GrantedPermissions,
        // Bluetooth
        WantsToTurnBluetoothOn,
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
            userPromptStates,
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
                            UserPromptState.DeniedPermissions -> flowOf(State.DeniedPermissions)
                            else -> flowOf(State.RequestPermissions(permissionsState.permissions))
                        }
                    }
                    bluetoothState !is BluetoothState.On -> {
                        when (userPromptState) {
                            UserPromptState.DeniedTurningBluetoothOn -> flowOf(State.DeniedEnablingBluetooth)
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
        userPromptStates.value = UserPromptState.DeniedPermissions
    }

    fun userGrantedPermissions() {
        userPromptStates.value = UserPromptState.GrantedPermissions
    }

    fun userWantsToGrantPermissions() {
        userPromptStates.value = UserPromptState.WantsToGrantPermissions
    }

    fun userDeniedTurningBluetoothOn() {
        userPromptStates.value = UserPromptState.DeniedTurningBluetoothOn
    }

    fun userWantsToTurnBluetoothOn() {
        userPromptStates.value = UserPromptState.WantsToTurnBluetoothOn
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
