package nick.template.data.bluetooth.scanning

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import nick.template.data.bluetooth.BluetoothIdentifiers
import javax.inject.Inject

interface ScanningConfig {
    val filters: List<ScanFilter>?
    val scanSettings: ScanSettings
}

class AppScanningConfig @Inject constructor(
    bluetoothIdentifiers: BluetoothIdentifiers
) : ScanningConfig {
    override val filters: List<ScanFilter> = listOf(
        ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(bluetoothIdentifiers.service))
            .build()
    )

    override val scanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .build()
}
