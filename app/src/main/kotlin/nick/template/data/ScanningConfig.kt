package nick.template.data

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.seconds

interface ScanningConfig {
    val filters: List<ScanFilter>?
    val scanSettings: ScanSettings
    val scanDuration: Duration?
}

class BatchedScanningConfig @Inject constructor() : ScanningConfig {
    override val filters: List<ScanFilter>? = null

    override val scanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(400L)
        .build()

    override val scanDuration: Duration = 5.seconds
}
