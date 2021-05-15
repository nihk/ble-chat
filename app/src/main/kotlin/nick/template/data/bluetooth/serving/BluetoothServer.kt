package nick.template.data.bluetooth.serving

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import nick.template.data.offerSafely

interface BluetoothServer {
    fun events(): Flow<Event>

    sealed class Event {
        data class Connected(
            val address: String,
            val name: String?
        ) : Event()
        object Disconnected : Event()
        data class Message(
            val address: String,
            val message: String
        ) : Event()
    }
}

class AndroidBluetoothServer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val bluetoothGattService: BluetoothGattService
) : BluetoothServer {
    private var gattServer: BluetoothGattServer? = null

    override fun events(): Flow<BluetoothServer.Event> = callbackFlow {
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
                characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                // fixme: does this need to be a part of the queue?
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8) ?: return

                val event = BluetoothServer.Event.Message(
                    address = device.address,
                    message = message
                )

                offerSafely(event)
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
