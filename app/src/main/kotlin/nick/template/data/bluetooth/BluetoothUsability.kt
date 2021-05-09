package nick.template.data.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import nick.template.data.LocationState
import nick.template.data.LocationStates
import nick.template.data.bluetooth.BluetoothUsability.SideEffect
import javax.inject.Inject

interface BluetoothUsability {
    fun sideEffects(): Flow<SideEffect>
    suspend fun checkUsability()

    sealed class SideEffect {
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
    private val triggers = MutableSharedFlow<Unit>()

    // Counts to avoid spamming the user with requests. UI can act accordingly based on
    // how many times the user has been prompted with requests/asks.
    private var requestPermissionsCount = 0
    private var askToTurnBluetoothOnCount = 0
    private var askToTurnLocationOnCount = 0

    override fun sideEffects(): Flow<SideEffect> {
        return combine(
            triggers.onStart { emit(Unit) },
            bluetoothStates.states(),
            locationStates.states()
        ) { _, bluetoothState, locationState ->
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

    override suspend fun checkUsability() {
        triggers.emit(Unit)
    }
}
