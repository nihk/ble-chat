package nick.template.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface BluetoothScanner {
    fun results(): Flow<Result>

    // todo: consumer will probably need some kind of caching mechanism in case an error
    //  happens _after_ valid devices have been received - i.e. they'll still want to see
    //  the last known BT devices (probably?)
    sealed class Result {
        data class Devices(val addresses: List<String>) : Result()
        data class Error(val errorCode: Int) : Result()
        object BluetoothNotEnabled : Result()
    }
}

class AndroidBluetoothScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothStates: BluetoothStates
) : BluetoothScanner {

    override fun results(): Flow<BluetoothScanner.Result> {
        return bluetoothStates.states()
            .flatMapLatest { state ->
                if (state == BluetoothState.ON) {
                    val bluetoothLeScanner = requireNotNull(bluetoothAdapter.bluetoothLeScanner)
                    bluetoothLeScannerResults(bluetoothLeScanner)
                } else {
                    bluetoothNotEnabled()
                }
            }
    }

    // fixme: what happens when subscribed but user turns off BT?
    private fun bluetoothLeScannerResults(bluetoothLeScanner: BluetoothLeScanner) = callbackFlow<BluetoothScanner.Result> {
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val names = listOf(result.device.address)
                offer(BluetoothScanner.Result.Devices(names))
            }

            override fun onScanFailed(errorCode: Int) {
                offer(BluetoothScanner.Result.Error(errorCode))
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                val names = results.map { result -> result.device.address }
                offer(BluetoothScanner.Result.Devices(names))
            }
        }

        bluetoothLeScanner.startScan(callback)

        awaitClose { bluetoothLeScanner.stopScan(callback) }
    }

    private fun bluetoothNotEnabled(): Flow<BluetoothScanner.Result> {
        return flowOf(BluetoothScanner.Result.BluetoothNotEnabled)
    }
}
