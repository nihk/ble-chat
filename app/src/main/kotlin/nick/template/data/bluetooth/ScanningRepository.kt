package nick.template.data.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import nick.template.data.Resource
import nick.template.data.local.Device
import nick.template.data.local.DeviceDao
import javax.inject.Inject

interface ScanningRepository {
    fun devices(): Flow<Resource<List<Device>>>
}

class DefaultScanningRepository @Inject constructor(
    private val bluetoothScanner: OneShotBluetoothScanner,
    private val deviceDao: DeviceDao,
    private val deviceCacheThreshold: DeviceCacheThreshold
) : ScanningRepository {
    override fun devices(): Flow<Resource<List<Device>>> = flow {
        emit(Resource.Loading())
        emit(Resource.Loading(deviceDao.selectAll().first()))

        val flow = when (val result = bluetoothScanner.result()) {
            is BluetoothScanner.Result.Error -> deviceDao.selectAll().map { devices -> Resource.Error(devices, result.error)}
            is BluetoothScanner.Result.Success -> {
                deviceDao.insertAndPurgeOldDevices(result.devices, deviceCacheThreshold.threshold)
                deviceDao.selectAll().map { devices -> Resource.Success(devices) }
            }
        }

        emitAll(flow)
    }
}
