package nick.template.data.bluetooth

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import javax.inject.Inject

interface AdvertiseConfig {
    val settings: AdvertiseSettings
    val data: AdvertiseData
}

class DefaultAdvertiseConfig @Inject constructor(
    bluetoothUuids: BluetoothUuids
) : AdvertiseConfig {
    override val settings: AdvertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setTimeout(0)
        .build()

    override val data: AdvertiseData = AdvertiseData.Builder()
        .addServiceUuid(ParcelUuid(bluetoothUuids.service))
        .setIncludeDeviceName(true)
        .build()
}
