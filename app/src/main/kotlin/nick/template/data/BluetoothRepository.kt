package nick.template.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface BluetoothRepository {
    fun bluetoothStates(): Flow<BluetoothState>
    fun scanningResults(): Flow<BluetoothScanner.Result>
    fun permissionsState(): BluetoothPermissions.State
}

class AndroidBluetoothRepository @Inject constructor(
    private val bluetoothStates: BluetoothStates,
    private val bluetoothScanner: BluetoothScanner,
    private val bluetoothPermissions: BluetoothPermissions
) : BluetoothRepository {
    override fun bluetoothStates(): Flow<BluetoothState> = bluetoothStates.states()
    override fun scanningResults(): Flow<BluetoothScanner.Result> = bluetoothScanner.results()
    override fun permissionsState(): BluetoothPermissions.State = bluetoothPermissions.state()
}
