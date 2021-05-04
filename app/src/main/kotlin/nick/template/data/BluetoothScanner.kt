package nick.template.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

interface BluetoothScanner {
    fun results(): Flow<Result>

    sealed class Result {
        data class Devices(val addresses: List<String>) : Result()
        data class Error(val errorCode: Int) : Result()
    }
}

// Note: starting/stopping 5 or more times within a 30 second window will make the next startScan
// call silently fail.
class AndroidBluetoothScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val scanningConfig: ScanningConfig
) : BluetoothScanner {

    override fun results(): Flow<BluetoothScanner.Result> {
        val flow = callbackFlow<BluetoothScanner.Result> {
            val bluetoothLeScanner = requireNotNull(bluetoothAdapter.bluetoothLeScanner) {
                "Either BT wasn't turned on or relevant permissions weren't actively granted!"
            }
            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    if (isClosedForSend) return
                    val addresses = listOf(result.device.address)
                    offer(BluetoothScanner.Result.Devices(addresses))
                }

                override fun onScanFailed(errorCode: Int) {
                    if (isClosedForSend) return
                    offer(BluetoothScanner.Result.Error(errorCode))
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>) {
                    if (isClosedForSend) return
                    val addresses = results.map { result -> result.device.address }
                    offer(BluetoothScanner.Result.Devices(addresses))
                }
            }

            bluetoothLeScanner.startScan(scanningConfig.filters, scanningConfig.scanSettings, callback)

            awaitClose { bluetoothLeScanner.stopScan(callback) }
        }

        return flow
            .conflate()
            .onEach { delay(scanningConfig.emissionDelay.toLongMilliseconds()) }
    }
}
