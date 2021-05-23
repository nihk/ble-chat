package ble.usability

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import ble.LocationState
import ble.LocationStates
import ble.usability.BluetoothUsability.SideEffect
import javax.inject.Inject

interface BluetoothUsability {
    fun sideEffects(): Flow<SideEffect>
    suspend fun checkUsability()

    sealed class SideEffect {
        // Required as a signal that resubscription happened
        object Initial : SideEffect()
        object UseBluetooth : SideEffect()
        data class RequestPermissions(
            val permissions: List<String>,
            val numTimesRequestedPreviously: Int
        ) : SideEffect()
        data class AskToTurnBluetoothOn(val numTimesAskedPreviously: Int) : SideEffect()
        data class AskToTurnLocationOn(val numTimesAskedPreviously: Int) : SideEffect()
    }
}

class DefaultBluetoothUsability @Inject constructor(
    private val bluetoothStates: BluetoothStates,
    private val locationStates: LocationStates,
    private val bluetoothPermissions: BluetoothPermissions
) : BluetoothUsability {
    private val events = MutableSharedFlow<Unit>()

    // Counts can be used to avoid spamming the user with requests. UI can act accordingly based on
    // how many times the user has been prompted with requests/asks.
    private var requestPermissionsCount = 0
    private var askToTurnBluetoothOnCount = 0
    private var askToTurnLocationOnCount = 0

    override fun sideEffects(): Flow<SideEffect> {
        return events
            .onStart { emit(Unit) } // Kick things off as soon as sideEffects() is called
            .flatMapLatest {
                combine(bluetoothStates.states(), locationStates.states()) { bluetoothState, locationState ->
                    val permissionsState = bluetoothPermissions.state()
                    when {
                        permissionsState is BluetoothPermissions.State.MissingPermissions ->
                            SideEffect.RequestPermissions(permissionsState.permissions, requestPermissionsCount++)
                        bluetoothState !is BluetoothState.On -> SideEffect.AskToTurnBluetoothOn(askToTurnBluetoothOnCount++)
                        locationState is LocationState.Off -> SideEffect.AskToTurnLocationOn(askToTurnLocationOnCount++)
                        else -> SideEffect.UseBluetooth
                    }
                }
            }
            .onStart { emit(SideEffect.Initial) }
    }

    override suspend fun checkUsability() {
        events.emit(Unit)
    }
}
