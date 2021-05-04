package nick.template.data

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

interface BluetoothStates {
    fun states(): Flow<BluetoothState>
}

class AndroidBluetoothStates @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothStates {

    override fun states(): Flow<BluetoothState> {
        val bluetoothStates = callbackFlow<BluetoothState> {
            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) {
                        return
                    }

                    offer(bluetoothAdapter.state.toBluetoothState())
                }
            }

            context.registerReceiver(broadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

            awaitClose { context.unregisterReceiver(broadcastReceiver) }
        }

        return bluetoothStates.onStart { emit(bluetoothAdapter.state.toBluetoothState()) }
    }

    private fun Int.toBluetoothState(): BluetoothState {
        return when (this) {
            BluetoothAdapter.STATE_OFF -> BluetoothState.Off
            BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.TurningOff
            BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.TurningOn
            BluetoothAdapter.STATE_ON -> BluetoothState.On
            // BluetoothAdapter can sometimes emit hidden states (lol) on Android M.
            // See: https://github.com/iDevicesInc/SweetBlue/wiki/Android-BLE-Issues#android-m-issues
            else -> BluetoothState.Unknown(this)
        }
    }
}
