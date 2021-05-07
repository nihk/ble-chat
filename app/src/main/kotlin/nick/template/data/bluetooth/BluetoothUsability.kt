package nick.template.data.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

interface BluetoothUsability {
    fun events(): Flow<Event>
    suspend fun promptIfNeeded()
    suspend fun denyPermissions()
    suspend fun denyTurningBluetoothOn()

    sealed class Event {
        object CanUseBluetooth : Event()
        data class RequestPermissions(val permissions: List<String>) : Event()
        object InformPermissionsRequired : Event()
        object AskToTurnBluetoothOn : Event()
        object InformBluetoothRequired : Event()
    }
}

class AndroidBluetoothUsability @Inject constructor(
    private val bluetoothStates: BluetoothStates,
    private val bluetoothPermissions: BluetoothPermissions
) : BluetoothUsability {
    private val actions = MutableSharedFlow<Action>()

    private enum class Action {
        PromptIfNeeded,
        DenyPermissions,
        DenyTurningOnBluetooth
    }

    override fun events(): Flow<BluetoothUsability.Event> {
        return combine(
            actions.onStart { emit(Action.PromptIfNeeded) },
            bluetoothStates.states()
        ) { userPromptState, bluetoothState ->
            Pair(userPromptState, bluetoothState)
        }
            .map { pair ->
                val userAction = pair.first
                val bluetoothState = pair.second
                val permissionsState = bluetoothPermissions.state()
                when {
                    permissionsState is BluetoothPermissions.State.MissingPermissions -> {
                        when (userAction) {
                            Action.DenyPermissions -> BluetoothUsability.Event.InformPermissionsRequired
                            else -> BluetoothUsability.Event.RequestPermissions(permissionsState.permissions)
                        }
                    }
                    bluetoothState !is BluetoothState.On -> {
                        when (userAction) {
                            Action.DenyTurningOnBluetooth -> BluetoothUsability.Event.InformBluetoothRequired
                            else -> BluetoothUsability.Event.AskToTurnBluetoothOn
                        }
                    }
                    else -> BluetoothUsability.Event.CanUseBluetooth
                }
            }
    }

    override suspend fun promptIfNeeded() {
        actions.emit(Action.PromptIfNeeded)
    }

    override suspend fun denyPermissions() {
        actions.emit(Action.DenyPermissions)
    }

    override suspend fun denyTurningBluetoothOn() {
        actions.emit(Action.DenyTurningOnBluetooth)
    }
}
