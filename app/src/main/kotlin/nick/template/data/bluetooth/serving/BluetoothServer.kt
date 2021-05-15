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
import nick.template.data.bluetooth.ServiceDataConfig
import nick.template.data.offerSafely

interface BluetoothServer {
    fun events(): Flow<Event>

    sealed class Event {
        data class Connected(
            val address: String,
            val name: String?
        ) : Event()
        object Disconnected : Event()
        class Message(
            val identifier: ByteArray,
            val message: String
        ) : Event()
    }
}

class AndroidBluetoothServer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val bluetoothGattService: BluetoothGattService,
    private val serviceDataConfig: ServiceDataConfig
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
                val identifierPart = value.copyOfRange(0, serviceDataConfig.byteSize)
                val messagePart = value.copyOfRange(serviceDataConfig.byteSize, value.size)
                    .toString(Charsets.UTF_8)

                val event = BluetoothServer.Event.Message(
                    identifier = identifierPart,
                    message = messagePart
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
