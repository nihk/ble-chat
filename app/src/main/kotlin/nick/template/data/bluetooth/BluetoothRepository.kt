package nick.template.data.bluetooth

import kotlinx.coroutines.flow.Flow
import nick.template.data.local.Device
import nick.template.data.local.DeviceDao
import javax.inject.Inject

interface BluetoothRepository {
    fun bluetoothStates(): Flow<BluetoothState>
    fun scanningResults(): Flow<BluetoothScanner.Result>
    fun permissionsState(): BluetoothPermissions.State
    fun connect(device: Device): Flow<BluetoothConnector.State>
    fun devices(): Flow<Device>
}

class DelegatingBluetoothRepository @Inject constructor(
    private val bluetoothStates: BluetoothStates,
    private val bluetoothScanner: BluetoothScanner,
    private val bluetoothPermissions: BluetoothPermissions,
    private val bluetoothConnector: BluetoothConnector,
    private val deviceDao: DeviceDao
) : BluetoothRepository {
    override fun bluetoothStates(): Flow<BluetoothState> = bluetoothStates.states()
    override fun scanningResults(): Flow<BluetoothScanner.Result> = bluetoothScanner.results()
    override fun permissionsState(): BluetoothPermissions.State = bluetoothPermissions.state()
    override fun connect(device: Device): Flow<BluetoothConnector.State> = bluetoothConnector.connect(device)
    override fun devices(): Flow<Device> = deviceDao.selectAll()
}
