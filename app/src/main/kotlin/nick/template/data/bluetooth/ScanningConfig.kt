package nick.template.data.bluetooth

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.seconds

interface ScanningConfig : ScanningTimeout {
    val filters: List<ScanFilter>?
    val scanSettings: ScanSettings
}

interface ScanningTimeout {
    val timeout: Duration
}

class BatchedScanningConfig @Inject constructor() : ScanningConfig {
    override val filters: List<ScanFilter>? = null

    override val scanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setReportDelay(1_000L)
        .build()

    override val timeout: Duration = 5.seconds
}
