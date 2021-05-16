package ble.scanning

import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

interface ScanningTimeout {
    val timeout: Duration
}

class DefaultScanningTimeout @Inject constructor() : ScanningTimeout {
    override val timeout: Duration = 15.toDuration(DurationUnit.SECONDS)
}
