package ble.scanning

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import ble.BluetoothIdentifiers
import javax.inject.Inject

interface ScanningConfig {
    val filters: List<ScanFilter>?
    val scanSettings: ScanSettings
}

class DefaultScanningConfig @Inject constructor(
    bluetoothIdentifiers: BluetoothIdentifiers
) : ScanningConfig {
    override val filters: List<ScanFilter> = listOf(
        ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(bluetoothIdentifiers.service))
            .build()
    )

    override val scanSettings: ScanSettings = ScanSettings.Builder()
        // fixme: why does bulk scan not show device now / can't establish connection?
//        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
//        .setReportDelay(1_000L)
        .build()
}
