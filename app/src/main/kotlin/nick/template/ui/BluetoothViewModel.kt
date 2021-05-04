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

    private var canRequestPermissions = true
    private val manualTriggers = MutableStateFlow(Any())

    fun states(): Flow<State> {
        return combine(manualTriggers, repository.bluetoothStates()) { _, bluetoothState -> bluetoothState }
            .flatMapLatest { bluetoothState ->
                val permissionsState = repository.permissionsState()
                when {
                    permissionsState is BluetoothPermissions.State.MissingPermissions -> {
                        if (canRequestPermissions) {
                            canRequestPermissions = false
                            flowOf(State.RequestPermissions(permissionsState.permissions))
                        } else {
                            canRequestPermissions = true
                            flowOf(State.DeniedPermissions)
                        }
                    }
                    bluetoothState !is BluetoothState.On ->
                        flowOf(State.BluetoothIsntOn)
                    else -> repository.scanningResults()
                        .map { result ->
                            @Suppress("USELESS_CAST")
                            State.Scanned(result) as State
                        }
                        .onStart { emit(State.StartedScanning) }
                }
            }
    }

    fun retrigger() {
        manualTriggers.value = Any()
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
    object BluetoothIsntOn : State()
    object DeniedPermissions : State()
    object StartedScanning : State()
    data class Scanned(val result: BluetoothScanner.Result) : State()
}
