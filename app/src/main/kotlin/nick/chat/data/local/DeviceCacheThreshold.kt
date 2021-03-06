package nick.chat.data.local

import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import nick.chat.data.CurrentTime

interface DeviceCacheThreshold {
    val threshold: Long
}

class DefaultDeviceCacheThreshold @Inject constructor(
    private val currentTime: CurrentTime
) : DeviceCacheThreshold {
    private val gap = 30.toDuration(DurationUnit.SECONDS)

    override val threshold: Long
        get() = currentTime.millis() - gap.inWholeMilliseconds
}
