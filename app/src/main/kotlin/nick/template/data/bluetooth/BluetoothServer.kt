package nick.template.data.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import nick.template.data.CurrentTime
import nick.template.data.local.Device
import nick.template.data.offerSafely
import javax.inject.Inject

interface BluetoothServer {
    fun events(): Flow<Event>

    sealed class Event {
        data class Connected(val device: Device) : Event()
        object Disconnected : Event()
        data class Message(val message: String) : Event()
    }
}

class AndroidBluetoothServer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val bluetoothGattService: BluetoothGattService,
    private val currentTime: CurrentTime
) : BluetoothServer {

    override fun events(): Flow<BluetoothServer.Event> = callbackFlow {
        var gattServer: BluetoothGattServer? = null

        val callback = object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(
                device: BluetoothDevice,
                status: Int,
                newState: Int
            ) {
                val event = if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothGatt.STATE_CONNECTED) {
                    BluetoothServer.Event.Connected(device.toDevice())
                } else {
                    BluetoothServer.Event.Disconnected
                }

                offerSafely(event)
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8) ?: return

                offerSafely(BluetoothServer.Event.Message(message))
            }
        }

        gattServer = bluetoothManager.openGattServer(context, callback).apply {
            addService(bluetoothGattService)
        }

        awaitClose { gattServer?.close() }
    }

    private fun BluetoothDevice.toDevice(): Device {
        return Device(
            address = address,
            name = name,
            lastSeen = currentTime.millis()
        )
    }
}
