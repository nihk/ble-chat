package nick.template.data

sealed class BluetoothState {
    data class Unknown(val value: Int) : BluetoothState()
    object TurningOn : BluetoothState()
    object On : BluetoothState()
    object TurningOff : BluetoothState()
    object Off : BluetoothState()
}
