package ble

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

internal fun Int.toBluetoothError(): BluetoothError {
    return when (this) {
        else -> UnknownErrorCode(this)
    }
}
