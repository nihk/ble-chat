package nick.template.data

sealed class BluetoothState {
    object On : BluetoothState()
    object Other : BluetoothState()
}
