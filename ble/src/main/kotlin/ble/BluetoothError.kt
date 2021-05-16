package ble

import android.bluetooth.le.AdvertiseCallback
import kotlin.time.Duration

open class BluetoothError(override val message: String) : Throwable(message)

class UnknownErrorCode(val errorCode: Int) : BluetoothError(
    message = "An unknown error occurred. Its error code was: $errorCode"
)

class ScanningTimedOut(val duration: Duration) : BluetoothError(
    message = "Scanning timed out after $duration"
)

class AdvertisingNotSupported : BluetoothError(
    message = "Bluetooth advertising is not supported on this device"
)

class AdvertisingInternalError : BluetoothError(
    message = "Bluetooth advertising failed due to an internal error"
)

internal fun Int.toBluetoothError(): BluetoothError {
    return when (this) {
        AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> AdvertisingInternalError()
        else -> UnknownErrorCode(this)
    }
}
