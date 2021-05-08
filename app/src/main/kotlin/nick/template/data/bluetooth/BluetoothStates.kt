package nick.template.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import nick.template.data.offerSafely
import javax.inject.Inject

sealed class BluetoothState {
    object On : BluetoothState()
    object Other : BluetoothState()
}

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

                    offerSafely(bluetoothAdapter.state.toBluetoothState())
                }
            }

            context.registerReceiver(broadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

            awaitClose { context.unregisterReceiver(broadcastReceiver) }
        }

        return bluetoothStates
            .onStart { emit(bluetoothAdapter.state.toBluetoothState()) }
            .distinctUntilChanged()
    }

    private fun Int.toBluetoothState(): BluetoothState {
        return when (this) {
            BluetoothAdapter.STATE_ON -> BluetoothState.On
            else -> BluetoothState.Other
        }
    }
}
