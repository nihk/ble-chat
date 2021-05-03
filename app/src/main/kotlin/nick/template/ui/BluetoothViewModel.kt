package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class BluetoothViewModel : ViewModel() {

    private val states: MutableStateFlow<State> = MutableStateFlow(State.RequestPermissions)
    fun states(): Flow<State> = states

    fun setPermissionsResult(gotPermissions: Boolean) {
        states.value = if (gotPermissions) {
            State.GotPermissions
        } else {
            State.DeniedPermissions
        }
    }

    fun onRequestingPermissions() {
        states.value = State.RequestingPermissions
    }

    class Factory @Inject constructor() {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel?> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return BluetoothViewModel() as T
                }
            }
        }
    }
}

sealed class State {
    object RequestPermissions : State()
    object RequestingPermissions : State()
    object GotPermissions : State()
    object DeniedPermissions : State()
}
