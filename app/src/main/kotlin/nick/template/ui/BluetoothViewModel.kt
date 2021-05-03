package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import nick.template.data.BluetoothRepository
import nick.template.data.BluetoothScanner
import javax.inject.Inject

class BluetoothViewModel(
    private val repository: BluetoothRepository
) : ViewModel() {

    private val permissionsStates = MutableStateFlow(PermissionsState.RequestPermissions)
    val states = combine(permissionsStates, repository.scanningResults()) { permissionState, scanResult ->
        State(
            permissionsState = permissionState,
            scanResult = scanResult
        )
    }

    fun setPermissionsResult(gotPermissions: Boolean) {
        permissionsStates.value = if (gotPermissions) {
            PermissionsState.GotPermissions
        } else {
            PermissionsState.DeniedPermissions
        }
    }

    fun onRequestingPermissions() {
        permissionsStates.value = PermissionsState.RequestingPermissions
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

data class State(
    val scanResult: BluetoothScanner.Result,
    val permissionsState: PermissionsState
)

enum class PermissionsState {
    RequestPermissions,
    RequestingPermissions,
    GotPermissions,
    DeniedPermissions
}
