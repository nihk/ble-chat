package nick.template.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import nick.template.data.resumeSafely
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.milliseconds

data class Advertisement(
    val powerLevel: Int,
    val timeout: Duration
)

interface BluetoothAdvertiser {
    suspend fun start(): Result

    sealed class Result {
        data class Success(val advertisement: Advertisement) : Result()
        data class Error(val error: BluetoothError) : Result()
    }
}

class AndroidBluetoothAdvertiser @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val advertiseConfig: AdvertiseConfig
): BluetoothAdvertiser {
    override suspend fun start(): BluetoothAdvertiser.Result = suspendCancellableCoroutine { continuation ->
        val advertiser = requireBle(bluetoothAdapter.bluetoothLeAdvertiser)
        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                continuation.resumeSafely(BluetoothAdvertiser.Result.Success(settingsInEffect.toAdvertisement()))
            }

            override fun onStartFailure(errorCode: Int) {
                continuation.resumeSafely(BluetoothAdvertiser.Result.Error(errorCode.toBluetoothError()))
            }
        }

        advertiser.startAdvertising(
            advertiseConfig.settings,
            advertiseConfig.data,
            callback
        )

        continuation.invokeOnCancellation { advertiser.stopAdvertising(callback) }
    }

    private fun AdvertiseSettings.toAdvertisement(): Advertisement {
        return Advertisement(
            powerLevel = txPowerLevel,
            timeout = timeout.milliseconds
        )
    }
}
