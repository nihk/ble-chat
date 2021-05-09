package nick.template.data.bluetooth

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AdvertisingRepository {
    fun advertise(): Flow<BluetoothAdvertiser.StartResult>
}

class DefaultAdvertisingRepository @Inject constructor(
    private val bluetoothAdvertiser: BluetoothAdvertiser
) : AdvertisingRepository {
    override fun advertise(): Flow<BluetoothAdvertiser.StartResult> {
        return bluetoothAdvertiser.start()
    }
}
