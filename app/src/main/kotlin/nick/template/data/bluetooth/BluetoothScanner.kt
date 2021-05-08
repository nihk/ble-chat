package nick.template.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import nick.template.data.CurrentTime
import nick.template.data.local.Device
import nick.template.data.offerSafely
import javax.inject.Inject

interface BluetoothScanner {
    fun results(): Flow<Result>

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

    override fun results(): Flow<BluetoothScanner.Result> = callbackFlow {
        val bluetoothLeScanner = requireNotNull(bluetoothAdapter.bluetoothLeScanner) {
            "Either BT wasn't turned on or relevant permissions weren't actively granted!"
        }

        // BluetoothLeScanner.stopScan() is actually an asynchronous operation, so these callbacks
        // can still get hit after stopScan() is invoked.
        // SendChannel.offerSafely() is used to reconcile that.
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val devices = listOf(result.toDevice())
                offerSafely(BluetoothScanner.Result.Success(devices))
            }

            override fun onScanFailed(errorCode: Int) {
                offerSafely(BluetoothScanner.Result.Error(errorCode))
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                if (results.isEmpty()) return

                val devices = results.map { result -> result.toDevice() }
                offerSafely(BluetoothScanner.Result.Success(devices))
            }
        }

        bluetoothLeScanner.startScan(
            scanningConfig.filters,
            scanningConfig.scanSettings,
            callback
        )

        awaitClose { bluetoothLeScanner.stopScan(callback) }
    }

    private fun ScanResult.toDevice(): Device {
        return Device(
            address = device.address,
            name = device.name,
            lastSeen = currentTime.millis()
        )
    }
}
