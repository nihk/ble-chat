package nick.template.data

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import javax.inject.Inject

interface ScanningConfig {
    val filters: List<ScanFilter>?
    val scanSettings: ScanSettings
}

class DefaultScanningConfig @Inject constructor() : ScanningConfig {
    override val filters: List<ScanFilter>? = null

    override val scanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setReportDelay(2_500L)
        .build()
}
