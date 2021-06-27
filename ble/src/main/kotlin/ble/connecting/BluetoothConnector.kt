package ble.connecting

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import ble.BluetoothIdentifiers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ble.offerSafely
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose

interface BluetoothConnector {
    fun connect(address: String): Flow<State>
    fun send(data: String)

    sealed class State {
        object Initial : State()
        object Connected : State()
        data class Error(val status: Int) : State()
        object DiscoveredServices : State()
        class CharacteristicWritten(val data: ByteArray): State()
    }
}

/*
    todo: every callback needs to be in a try/catch/finally block, if a queueing mechnanism is to be used
 */
class AndroidBluetoothConnector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothIdentifiers: BluetoothIdentifiers
) : BluetoothConnector {
    private var characteristic: BluetoothGattCharacteristic? = null
    private var bluetoothGatt: BluetoothGatt? = null

    override fun connect(address: String) = callbackFlow {
        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                Log.d("asdf", "onConnectionStateChange, status: $status")
                if (newState != BluetoothProfile.STATE_CONNECTED || status != BluetoothGatt.GATT_SUCCESS) {
                    offerSafely(BluetoothConnector.State.Error(status))
                    return
                }

                offerSafely(BluetoothConnector.State.Connected)
                gatt.discoverServices()
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                Log.d("asdf", "onServicesDiscovered, status: $status")
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    return
                }

                offerSafely(BluetoothConnector.State.DiscoveredServices)
                val service = gatt.getService(bluetoothIdentifiers.service)
                characteristic = service.getCharacteristic(bluetoothIdentifiers.message)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                Log.d("asdf", "onCharacteristicWrite, status: $status")
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    return
                }

                offerSafely(BluetoothConnector.State.CharacteristicWritten(characteristic.value))
            }
        }

        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
        Log.d("asdf", "connecting to address: $address")

        // todo: emit?
        bluetoothGatt = bluetoothDevice.connectGatt(
            context, false, callback
        )

        offerSafely(BluetoothConnector.State.Initial)

        awaitClose {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            characteristic = null
            bluetoothGatt = null
        }
    }

    override fun send(data: String) {
        val characteristic = characteristic ?: return
        val bluetoothGatt = bluetoothGatt ?: return

        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        characteristic.value = bluetoothIdentifiers.serviceData + data.toByteArray(Charsets.UTF_8)

        val success = bluetoothGatt.writeCharacteristic(characteristic)
        if (!success) {
            Log.d("asdf", "Failed to send data: $data")
        }
    }
}
