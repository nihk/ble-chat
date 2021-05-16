package ble.scanning

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.ParcelUuid
import ble.BluetoothError
import ble.offerSafely
import ble.requireBle
import ble.toBluetoothError
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface BluetoothScanner {
    fun results(): Flow<Result>

    sealed class Result {
        data class Success(val scans: List<Scan>) : Result()
        data class Error(val error: BluetoothError) : Result()
    }
}

class AndroidBluetoothScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val scanningConfig: ScanningConfig
) : BluetoothScanner {

    override fun results(): Flow<BluetoothScanner.Result> = callbackFlow {
        val bluetoothLeScanner = requireBle(bluetoothAdapter.bluetoothLeScanner)

        // BluetoothLeScanner.stopScan() is actually an asynchronous operation, so these callbacks
        // can still get hit after stopScan() is invoked.
        // SendChannel.offerSafely() is used to reconcile that.
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val scans = listOf(result.toScan())
                offerSafely(BluetoothScanner.Result.Success(scans))
            }

            override fun onScanFailed(errorCode: Int) {
                offerSafely(BluetoothScanner.Result.Error(errorCode.toBluetoothError()))
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                if (results.isEmpty()) return

                val scans = results.map { result -> result.toScan() }
                offerSafely(BluetoothScanner.Result.Success(scans))
            }
        }

        bluetoothLeScanner.startScan(
            scanningConfig.filters,
            scanningConfig.scanSettings,
            callback
        )

        awaitClose { bluetoothLeScanner.stopScan(callback) }
    }

    private fun ScanResult.toScan(): Scan {
        return Scan(
            address = device.address,
            name = device.name,
            services = scanRecord?.serviceUuids
                .orEmpty()
                .associate { parcelUuid: ParcelUuid ->
                    parcelUuid.uuid to scanRecord?.getServiceData(parcelUuid)
                }
        )
    }
}
