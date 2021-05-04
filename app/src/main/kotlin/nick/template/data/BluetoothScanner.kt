package nick.template.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

interface BluetoothScanner {
    fun results(): Flow<Result>

    sealed class Result {
        data class Devices(val addresses: List<String>) : Result()
        data class Error(val errorCode: Int) : Result()
    }
}

// NB: Subscribing to results() assumes bluetooth is on and relevant permissions are actively granted.
class AndroidBluetoothScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothScanner {

    override fun results(): Flow<BluetoothScanner.Result> {
        val flow = callbackFlow<BluetoothScanner.Result> {
            val bluetoothLeScanner = requireNotNull(bluetoothAdapter.bluetoothLeScanner)
            // fixme: should this been scanning then stopping after a period of time?
            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val addresses = listOf(result.device.address)
                    // fixme: another github repo used this.isClosedForSend to guard these offers
                    offer(BluetoothScanner.Result.Devices(addresses))
                }

                override fun onScanFailed(errorCode: Int) {
                    offer(BluetoothScanner.Result.Error(errorCode))
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>) {
                    val addresses = results.map { result -> result.device.address }
                    offer(BluetoothScanner.Result.Devices(addresses))
                }
            }

            Log.d("asdf", "starting scan")
            bluetoothLeScanner.startScan(callback)

            awaitClose {
                Log.d("asdf", "stopping scan")
                bluetoothLeScanner.stopScan(callback)
            }
        }

        return flow
            // Scan results are spammy
        // Fixme: why is this crashing coroutines?
        /*
        AndroidRuntime: FATAL EXCEPTION: main
    Process: nick.bluetooth_fun, PID: 28578
    kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelled}@127a935
         */
//            .conflate()
//            .onEach { delay(500L) }
    }
}
