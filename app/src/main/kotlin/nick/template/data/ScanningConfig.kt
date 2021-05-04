package nick.template.data

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.milliseconds

interface ScanningConfig {
    val filters: List<ScanFilter>?
    val scanSettings: ScanSettings
    val emissionDelay: Duration
}

class DefaultScanningConfig @Inject constructor() : ScanningConfig {
    override val filters: List<ScanFilter>? = null

    override val scanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .build()

    override val emissionDelay: Duration = 400.milliseconds
}
