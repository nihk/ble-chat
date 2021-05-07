package nick.template.data.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import nick.template.data.bluetooth.BluetoothUsability.Event
import nick.template.data.bluetooth.BluetoothUsability.SideEffect
import nick.template.data.bluetooth.BluetoothUsability.State
import nick.template.data.bluetooth.BluetoothUsability.Transition
import javax.inject.Inject

interface BluetoothUsability {
    fun transitions(): Flow<Transition>
    suspend fun event(event: Event)

    sealed class Event {
        object PromptIfNeeded : Event()
        object DenyPermissions : Event()
        object DenyTurningBluetoothOn : Event()
    }

    sealed class State {
        object CanUseBluetooth : State()
        object CannotUseBluetooth : State()
    }

    sealed class SideEffect {
        object StartScanning : SideEffect()
        data class RequestPermissions(val permissions: List<String>) : SideEffect()
        object InformPermissionsRequired : SideEffect()
        object AskToTurnBluetoothOn : SideEffect()
        object InformBluetoothRequired : SideEffect()
    }

    data class Transition(
        val state: State,
        val sideEffect: SideEffect
    )
}

class DefaultBluetoothUsability @Inject constructor(
    private val bluetoothStates: BluetoothStates,
    private val bluetoothPermissions: BluetoothPermissions
) : BluetoothUsability {
    private val events = MutableSharedFlow<Event>()

    override fun transitions(): Flow<Transition> {
        return combine(
            events.onStart { emit(Event.PromptIfNeeded) },
            bluetoothStates.states()
        ) { event, bluetoothState ->
            val permissionsState = bluetoothPermissions.state()
            when {
                permissionsState is BluetoothPermissions.State.MissingPermissions -> {
                    when (event) {
                        Event.DenyPermissions -> Transition(
                            State.CannotUseBluetooth,
                            SideEffect.InformPermissionsRequired
                        )
                        else -> Transition(
                            State.CannotUseBluetooth,
                            SideEffect.RequestPermissions(permissionsState.permissions)
                        )
                    }
                }
                bluetoothState !is BluetoothState.On -> {
                    when (event) {
                        Event.DenyTurningBluetoothOn -> Transition(
                            State.CannotUseBluetooth,
                            SideEffect.InformBluetoothRequired
                        )
                        else -> Transition(
                            State.CannotUseBluetooth,
                            SideEffect.AskToTurnBluetoothOn
                        )
                    }
                }
                else -> Transition(State.CanUseBluetooth, SideEffect.StartScanning)
            }
        }
    }

    override suspend fun event(event: Event) {
        events.emit(event)
    }
}
