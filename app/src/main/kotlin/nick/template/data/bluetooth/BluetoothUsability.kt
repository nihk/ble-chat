package nick.template.data.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import nick.template.data.LocationState
import nick.template.data.LocationStates
import nick.template.data.bluetooth.BluetoothUsability.Event
import nick.template.data.bluetooth.BluetoothUsability.SideEffect
import javax.inject.Inject

interface BluetoothUsability {
    fun sideEffects(): Flow<SideEffect>
    suspend fun handleEvent(event: Event)

    sealed class Event {
        object PromptIfNeeded : Event()
        object DenyPermissions : Event()
        object DenyTurningBluetoothOn : Event()
        object DenyTurningLocationOn : Event()
    }

    sealed class SideEffect {
        object UseBluetooth : SideEffect()
        data class RequestPermissions(val permissions: List<String>) : SideEffect()
        object InformPermissionsRequired : SideEffect()
        object AskToTurnBluetoothOn : SideEffect()
        object InformBluetoothRequired : SideEffect()
        object AskToTurnLocationOn : SideEffect()
        object InformLocationRequired : SideEffect()
    }
}

class DefaultBluetoothUsability @Inject constructor(
    private val bluetoothStates: BluetoothStates,
    private val locationStates: LocationStates,
    private val bluetoothPermissions: BluetoothPermissions
) : BluetoothUsability {
    private val events = MutableSharedFlow<Event>()

    // todo: decouple this function from stateful Events - use a new class
    // todo: can i stash a timestamp in AskTo* SideEffects and let VM/UI decide what to do?
    override fun sideEffects(): Flow<SideEffect> {
        return combine(
            events.onStart { emit(Event.PromptIfNeeded) },
            bluetoothStates.states(),
            locationStates.states()
        ) { event, bluetoothState, locationState ->
            val permissionsState = bluetoothPermissions.state()
            when {
                permissionsState is BluetoothPermissions.State.MissingPermissions -> {
                    when (event) {
                        Event.DenyPermissions -> SideEffect.InformPermissionsRequired
                        else -> SideEffect.RequestPermissions(permissionsState.permissions)
                    }
                }
                bluetoothState !is BluetoothState.On -> {
                    when (event) {
                        Event.DenyTurningBluetoothOn -> SideEffect.InformBluetoothRequired
                        else -> SideEffect.AskToTurnBluetoothOn
                    }
                }
                locationState is LocationState.Off -> {
                    when (event) {
                        Event.DenyTurningLocationOn -> SideEffect.InformLocationRequired
                        else -> SideEffect.AskToTurnLocationOn
                    }
                }
                else -> SideEffect.UseBluetooth
            }
        }
    }

    override suspend fun handleEvent(event: Event) {
        events.emit(event)
    }
}
