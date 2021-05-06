package nick.template.data.bluetooth

sealed class BluetoothState {
    object On : BluetoothState()
    object Other : BluetoothState()
}
