package ble

import ble.serving.BluetoothServer

interface CharacteristicParser {
    fun parse(value: ByteArray): BluetoothServer.Event.Message
}
