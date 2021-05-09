package nick.template.data.bluetooth

import java.util.UUID
import javax.inject.Inject

interface BluetoothUuids {
    val service: UUID
    val message: UUID
    val confirmConnection: UUID
}

class AppBluetoothUuids @Inject constructor() : BluetoothUuids {
    override val service: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
    override val message: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
    override val confirmConnection: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")
}
