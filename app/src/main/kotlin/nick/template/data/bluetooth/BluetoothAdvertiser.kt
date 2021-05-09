package nick.template.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import nick.template.data.offerSafely
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.milliseconds

data class Advertisement(
    val powerLevel: Int,
    val timeout: Duration
)

interface BluetoothAdvertiser {
    suspend fun start(): Flow<StartResult>

    sealed class StartResult {
        data class Success(val advertisement: Advertisement) : StartResult()
        data class Error(val error: BluetoothError) : StartResult()
    }
}

class AndroidBluetoothAdvertiser @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val advertiseConfig: AdvertiseConfig
): BluetoothAdvertiser {
    override suspend fun start(): Flow<BluetoothAdvertiser.StartResult> = callbackFlow {
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
            advertiseConfig.settings,
            advertiseConfig.data,
            callback
        )

        awaitClose { advertiser.stopAdvertising(callback) }
    }

    private fun AdvertiseSettings.toAdvertisement(): Advertisement {
        return Advertisement(
            powerLevel = txPowerLevel,
            timeout = timeout.milliseconds
        )
    }
}
