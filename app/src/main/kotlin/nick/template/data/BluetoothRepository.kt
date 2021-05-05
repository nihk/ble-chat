package nick.template.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface BluetoothRepository {
    fun bluetoothStates(): Flow<BluetoothState>
    fun scanningResults(): Flow<BluetoothScanner.Result>
    fun permissionsState(): BluetoothPermissions.State
    fun connect(device: Device): Flow<BluetoothConnector.State>
}

class DefaultBluetoothRepository @Inject constructor(
    private val bluetoothStates: BluetoothStates,
    private val bluetoothScanner: BluetoothScanner,
    private val bluetoothPermissions: BluetoothPermissions,
    private val bluetoothConnector: BluetoothConnector
) : BluetoothRepository {
    override fun bluetoothStates(): Flow<BluetoothState> = bluetoothStates.states()
    override fun scanningResults(): Flow<BluetoothScanner.Result> = bluetoothScanner.results()
    override fun permissionsState(): BluetoothPermissions.State = bluetoothPermissions.state()
    override fun connect(device: Device): Flow<BluetoothConnector.State> = bluetoothConnector.connect(device)
}
