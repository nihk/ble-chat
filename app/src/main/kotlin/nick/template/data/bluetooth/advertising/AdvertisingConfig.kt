package nick.template.data.bluetooth.advertising

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import javax.inject.Inject
import nick.template.data.bluetooth.BluetoothUuids

interface AdvertisingConfig {
    val settings: AdvertiseSettings
    val data: AdvertiseData
}

class DefaultAdvertisingConfig @Inject constructor(
    private val bluetoothUuids: BluetoothUuids
) : AdvertisingConfig {
    override val settings: AdvertiseSettings = AdvertiseSettings.Builder()
        .build()

    override val data: AdvertiseData
        get() {
            val parcelUuid = ParcelUuid(bluetoothUuids.service)
            return AdvertiseData.Builder()
                .addServiceUuid(parcelUuid)
                .addServiceData(parcelUuid, bluetoothUuids.serviceData)
                .setIncludeDeviceName(true)
                .build()
        }
}
