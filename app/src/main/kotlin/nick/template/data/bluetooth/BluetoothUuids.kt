package nick.template.data.bluetooth

import android.content.SharedPreferences
import java.nio.ByteBuffer
import java.util.UUID
import javax.inject.Inject
import nick.template.data.toByteArray

interface BluetoothUuids {
    val service: UUID
    val message: UUID
    val confirmConnection: UUID
    val serviceData: ByteArray
}

class AppBluetoothUuids @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val serviceDataConfig: ServiceDataConfig
) : BluetoothUuids {
    override val service: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
    override val message: UUID = run {
        var uuid = sharedPreferences.getString("message_uuid", null)?.toUuid()
        if (uuid == null) {
            uuid = UUID.randomUUID()
            sharedPreferences.edit()
                .putString("message_uuid", uuid.toString())
                .apply()
        }
        uuid!!
    }
    // todo: what is this even for?
    override val confirmConnection: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")
    override val serviceData: ByteArray = message.toByteArray(serviceDataConfig.byteSize)

    private fun String.toUuid(): UUID {
        return UUID.fromString(this)
    }
}
