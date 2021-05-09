package nick.template.data.bluetooth

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import javax.inject.Inject

interface ScanningConfig {
    val filters: List<ScanFilter>?
    val scanSettings: ScanSettings
}

class AppScanningConfig @Inject constructor(
    bluetoothUuids: BluetoothUuids
) : ScanningConfig {
    override val filters: List<ScanFilter> = listOf(
        ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(bluetoothUuids.service))
            .build()
    )

    override val scanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .build()
}
