package ble.serving

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import ble.offerSafely
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface BluetoothServer {
    fun events(): Flow<Event>

    sealed class Event {
        data class Connected(
            val address: String,
            val name: String?
        ) : Event()
        object Disconnected : Event()
        class Write(val value: ByteArray) : Event()
    }
}

class AndroidBluetoothServer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val bluetoothGattService: BluetoothGattService
) : BluetoothServer {

    override fun events(): Flow<BluetoothServer.Event> = callbackFlow {
        var gattServer: BluetoothGattServer? = null

        val callback = object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(
                device: BluetoothDevice,
                status: Int,
                newState: Int
            ) {
                val event = if (
                    status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothGatt.STATE_CONNECTED
                ) {
                    BluetoothServer.Event.Connected(
                        address = device.address,
                        name = device.name
                    )
                } else {
                    BluetoothServer.Event.Disconnected
                }

                offerSafely(event)
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                if (responseNeeded) {
                    // fixme: does this need to be a part of a queue?
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }

                val write = BluetoothServer.Event.Write(value)

                offerSafely(write)
            }
        }

        gattServer = bluetoothManager.openGattServer(context, callback).apply {
            addService(bluetoothGattService)
        }

        awaitClose {
            gattServer?.close()
            gattServer = null
        }
    }
}
