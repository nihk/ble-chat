package ble.advertising

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import javax.inject.Inject
import ble.BluetoothIdentifiers

interface AdvertisingConfig {
    val settings: AdvertiseSettings
    val data: AdvertiseData
}

class DefaultAdvertisingConfig @Inject constructor(
    private val bluetoothIdentifiers: BluetoothIdentifiers
) : AdvertisingConfig {
    override val settings: AdvertiseSettings = AdvertiseSettings.Builder()
        .build()

    override val data: AdvertiseData
        get() {
            val parcelUuid = ParcelUuid(bluetoothIdentifiers.service)
            return AdvertiseData.Builder()
                .addServiceUuid(parcelUuid)
                .addServiceData(parcelUuid, bluetoothIdentifiers.serviceData)
                .setIncludeDeviceName(true)
                .build()
        }
}
