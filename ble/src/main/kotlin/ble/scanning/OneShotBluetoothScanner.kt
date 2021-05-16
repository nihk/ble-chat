package ble.scanning

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import ble.ScanningTimedOut
import javax.inject.Inject

interface OneShotBluetoothScanner {
    suspend fun result(): BluetoothScanner.Result
}

class FirstResultBluetoothScanner @Inject constructor(
    private val bluetoothScanner: BluetoothScanner,
    private val scanningTimeout: ScanningTimeout
) : OneShotBluetoothScanner {
    override suspend fun result(): BluetoothScanner.Result {
        return withTimeoutOrNull(scanningTimeout.timeout) {
            bluetoothScanner.results()
                .first { result ->
                    result is BluetoothScanner.Result.Error
                        || result is BluetoothScanner.Result.Success && result.scans.isNotEmpty()
                }
        } ?: BluetoothScanner.Result.Error(ScanningTimedOut(scanningTimeout.timeout))
    }
}
