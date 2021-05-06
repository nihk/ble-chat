package nick.template.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import nick.template.data.CurrentTime
import nick.template.data.local.Device
import nick.template.data.offerSafely
import javax.inject.Inject

interface BluetoothScanner {
    fun results(): Flow<Result>

    sealed class Result {
        data class Success(val devices: List<Device>) : Result()
        data class Error(val errorCode: Int) : Result()
        object StoppedScanning : Result()
    }
}

// Note: starting/stopping 5 or more times within a 30 second window will make the next startScan
// call silently fail indefinitely. TODO: Can that be reconciled in the results() flow?
// Scanning continuously for 30+ minutes will change scanning to opportunistic (effectively
// stopping it). TODO: Reconcile this, too.
class AndroidBluetoothScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val scanningConfig: ScanningConfig,
    private val currentTime: CurrentTime
) : BluetoothScanner {

    override fun results(): Flow<BluetoothScanner.Result> = callbackFlow {
        val bluetoothLeScanner = requireNotNull(bluetoothAdapter.bluetoothLeScanner) {
            "Either BT wasn't turned on or relevant permissions weren't actively granted!"
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val bluetoothDevices = listOf(result.toDevice())
                offerSafely(BluetoothScanner.Result.Success(bluetoothDevices))
            }

            override fun onScanFailed(errorCode: Int) {
                offerSafely(BluetoothScanner.Result.Error(errorCode))
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                val bluetoothDevices = results.map { result -> result.toDevice() }
                // todo: consider closing the Flow here.
                offerSafely(BluetoothScanner.Result.Success(bluetoothDevices))
            }
        }

        bluetoothLeScanner.startScan(scanningConfig.filters, scanningConfig.scanSettings, callback)

        scanningConfig.scanDuration?.let { scanDuration ->
            launch {
                delay(scanDuration)
                offerSafely(BluetoothScanner.Result.StoppedScanning)
                close()
            }
        }

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
