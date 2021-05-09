package nick.template.data.bluetooth.connecting

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import nick.template.data.local.Device
import nick.template.data.offerSafely
import javax.inject.Inject

interface BluetoothConnector {
    fun connect(device: Device): Flow<State>

    sealed class State {
        object Server : State()
    }
}

/*
    todo: every callback needs to be in a try/catch/finally block, if a queueing mechnanism is to be used
 */
class AndroidBluetoothConnector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothConnector {
    override fun connect(device: Device) = callbackFlow<BluetoothConnector.State> {
        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Proceed to service discovery
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {

            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {

            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {

            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int
            ) {

            }
        }

        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.address)

        val server = bluetoothDevice.connectGatt(
            context, false, callback
        )

        offerSafely(BluetoothConnector.State.Server)
    }
}
