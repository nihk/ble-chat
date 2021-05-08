package nick.template.data.bluetooth

import kotlin.time.Duration

open class BluetoothError(override val message: String) : Throwable(message)

class UnknownErrorCode(val errorCode: Int) : BluetoothError(
    message = "An unknown error occurred. It's error code was: $errorCode"
)

class ScanningTimedOut(val duration: Duration) : BluetoothError(
    message = "Scanning timed out after $duration"
)
