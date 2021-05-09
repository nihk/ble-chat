package nick.template.data.bluetooth.scanning

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import nick.template.data.bluetooth.ScanningTimedOut
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
                        || result is BluetoothScanner.Result.Success && result.devices.isNotEmpty()
                }
        } ?: BluetoothScanner.Result.Error(ScanningTimedOut(scanningTimeout.timeout))
    }
}
