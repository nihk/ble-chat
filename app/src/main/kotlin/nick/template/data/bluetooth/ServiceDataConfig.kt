package nick.template.data.bluetooth

import javax.inject.Inject

interface ServiceDataConfig {
    val byteSize: Int
}

class DefaultServiceDataConfig @Inject constructor() : ServiceDataConfig {
    override val byteSize: Int = 4
}
