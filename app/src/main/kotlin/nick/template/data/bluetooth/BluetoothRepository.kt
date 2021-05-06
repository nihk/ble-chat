package nick.template.data.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import nick.template.data.local.Device
import nick.template.data.local.DeviceDao
import javax.inject.Inject

interface BluetoothRepository {
    fun bluetoothStates(): Flow<BluetoothState>
    fun permissionsState(): BluetoothPermissions.State
    fun connect(device: Device): Flow<BluetoothConnector.State>
    fun devices(): Flow<DevicesResource>
}

class DefaultBluetoothRepository @Inject constructor(
    private val bluetoothStates: BluetoothStates,
    private val bluetoothScanner: BluetoothScanner,
    private val bluetoothPermissions: BluetoothPermissions,
    private val bluetoothConnector: BluetoothConnector,
    private val deviceDao: DeviceDao
) : BluetoothRepository {
    override fun bluetoothStates(): Flow<BluetoothState> = bluetoothStates.states()
    override fun permissionsState(): BluetoothPermissions.State = bluetoothPermissions.state()
    override fun connect(device: Device): Flow<BluetoothConnector.State> = bluetoothConnector.connect(device)

    override fun devices(): Flow<DevicesResource> = flow {
        emit(DevicesResource.Loading())
        emit(DevicesResource.Loading(deviceDao.selectAll().first()))

        val flow = when (val result = bluetoothScanner.scan()) {
            is BluetoothScanner.Result.Error -> deviceDao.selectAll().map { devices -> DevicesResource.Error(devices, result.errorCode)}
            is BluetoothScanner.Result.Success -> {
                deviceDao.insertAndPurgeOldDevices(result.devices)
                deviceDao.selectAll().map { devices -> DevicesResource.Success(devices) }
            }
        }

        emitAll(flow)
    }
}

sealed class DevicesResource {
    abstract val devices: List<Device>?

    data class Loading(override val devices: List<Device>? = null) : DevicesResource()
    data class Success(override val devices: List<Device>) : DevicesResource()
    data class Error(override val devices: List<Device>? = null, val errorCode: Int) : DevicesResource()
}
