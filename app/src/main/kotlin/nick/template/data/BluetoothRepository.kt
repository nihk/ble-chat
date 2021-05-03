package nick.template.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface BluetoothRepository {
    fun states(): Flow<BluetoothState>
}

class AndroidBluetoothRepository @Inject constructor(
    private val bluetoothStates: BluetoothStates
) : BluetoothRepository {
    override fun states(): Flow<BluetoothState> = bluetoothStates.states()
}
