package nick.template.data

import android.bluetooth.BluetoothAdapter

enum class BluetoothState {
    TURNING_ON,
    ON,
    TURNING_OFF,
    OFF
}

fun Int.toBluetoothState(): BluetoothState {
    return when (this) {
        BluetoothAdapter.STATE_OFF -> BluetoothState.OFF
        BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.TURNING_OFF
        BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.TURNING_ON
        BluetoothAdapter.STATE_ON -> BluetoothState.ON
        else -> error("Unknown bluetooth state: $this")
    }
}
