package ble.advertising

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import ble.AdvertisingNotSupported
import ble.BluetoothError
import ble.offerSafely
import ble.requireBle
import ble.toBluetoothError
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class Advertisement(
    val powerLevel: Int,
    val timeout: Duration
)

interface BluetoothAdvertiser {
    fun start(): Flow<StartResult>

    sealed class StartResult {
        data class Success(val advertisement: Advertisement) : StartResult()
        data class Error(val error: BluetoothError) : StartResult()
    }
}

class AndroidBluetoothAdvertiser @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val advertisingConfig: AdvertisingConfig
): BluetoothAdvertiser {
    override fun start(): Flow<BluetoothAdvertiser.StartResult> = callbackFlow {
        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
            offerSafely(BluetoothAdvertiser.StartResult.Error(AdvertisingNotSupported()))
            close()
        }

        val advertiser = requireBle(bluetoothAdapter.bluetoothLeAdvertiser)

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                offerSafely(BluetoothAdvertiser.StartResult.Success(settingsInEffect.toAdvertisement()))
            }

            override fun onStartFailure(errorCode: Int) {
                offerSafely(BluetoothAdvertiser.StartResult.Error(errorCode.toBluetoothError()))
            }
        }

        advertiser.startAdvertising(
            advertisingConfig.settings,
            advertisingConfig.data,
            callback
        )

        awaitClose { advertiser.stopAdvertising(callback) }
    }

    private fun AdvertiseSettings.toAdvertisement(): Advertisement {
        return Advertisement(
            powerLevel = txPowerLevel,
            timeout = timeout.toDuration(DurationUnit.MILLISECONDS)
        )
    }
}
