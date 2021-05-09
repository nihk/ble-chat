package nick.template.data.bluetooth.scanning

import nick.template.data.CurrentTime
import javax.inject.Inject
import kotlin.time.seconds

interface DeviceCacheThreshold {
    val threshold: Long
}

class DefaultDeviceCacheThreshold @Inject constructor(
    private val currentTime: CurrentTime
) : DeviceCacheThreshold {
    private val gap = 30.seconds

    override val threshold: Long
        get() = currentTime.millis() - gap.toLongMilliseconds()
}
