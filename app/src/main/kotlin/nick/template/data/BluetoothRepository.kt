package nick.template.data

import android.bluetooth.BluetoothAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

interface BluetoothRepository {
    fun states(): Flow<BluetoothState>
}

class AndroidBluetoothRepository @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothStates: BluetoothStates
) : BluetoothRepository {
    override fun states(): Flow<BluetoothState> = bluetoothStates.states()
        .onStart { emit(bluetoothAdapter.state.toBluetoothState()) }
}
