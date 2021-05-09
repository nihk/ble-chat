package nick.template.data.bluetooth.advertising

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import nick.template.data.bluetooth.BluetoothUuids
import javax.inject.Inject

interface AdvertisingConfig {
    val settings: AdvertiseSettings
    val data: AdvertiseData
}

class DefaultAdvertisingConfig @Inject constructor(
    bluetoothUuids: BluetoothUuids
) : AdvertisingConfig {
    override val settings: AdvertiseSettings = AdvertiseSettings.Builder()
        .build()

    override val data: AdvertiseData = AdvertiseData.Builder()
        .addServiceUuid(ParcelUuid(bluetoothUuids.service))
        .setIncludeDeviceName(true)
        .build()
}
