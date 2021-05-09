package nick.template.data.bluetooth.scanning

import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.seconds

interface ScanningTimeout {
    val timeout: Duration
}

class DefaultScanningTimeout @Inject constructor() : ScanningTimeout {
    override val timeout: Duration = 15.seconds
}
