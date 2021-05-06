package nick.template.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.suspendCancellableCoroutine
import nick.template.data.CurrentTime
import nick.template.data.local.Device
import nick.template.data.resumeSafely
import javax.inject.Inject

interface BluetoothScanner {
    suspend fun scan(): Result

    sealed class Result {
        data class Success(val devices: List<Device>) : Result()
        data class Error(val errorCode: Int) : Result()
    }
}

class AndroidBluetoothScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val scanningConfig: ScanningConfig,
    private val currentTime: CurrentTime
) : BluetoothScanner {

    override suspend fun scan(): BluetoothScanner.Result = suspendCancellableCoroutine { continuation ->
        val bluetoothLeScanner = requireNotNull(bluetoothAdapter.bluetoothLeScanner) {
            "Either BT wasn't turned on or relevant permissions weren't actively granted!"
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                error("Use batched scan results only")
            }

            override fun onScanFailed(errorCode: Int) {
                bluetoothLeScanner.stopScan(this)
                continuation.resumeSafely(BluetoothScanner.Result.Error(errorCode))
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                if (results.isEmpty()) return

                bluetoothLeScanner.stopScan(this)
                val bluetoothDevices = results.map { result -> result.toDevice() }
                continuation.resumeSafely(BluetoothScanner.Result.Success(bluetoothDevices))
            }
        }

        bluetoothLeScanner.startScan(scanningConfig.filters, scanningConfig.scanSettings, callback)

        continuation.invokeOnCancellation { bluetoothLeScanner.stopScan(callback) }
    }

    private fun ScanResult.toDevice(): Device {
        return Device(
            address = device.address,
            name = device.name,
            lastSeen = currentTime.millis()
        )
    }
}
