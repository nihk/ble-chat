package nick.chat.bluetooth

import android.content.SharedPreferences
import ble.BluetoothIdentifiers
import ble.ServiceDataConfig
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random
import nick.chat.data.bytify
import nick.chat.data.stringify

class AppBluetoothIdentifiers @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val serviceDataConfig: ServiceDataConfig
) : BluetoothIdentifiers {
    override val service: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
    override val message: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
    override val serviceData: ByteArray = run {
        sharedPreferences.getString(KEY_SERVICE_DATA, null)?.bytify()
            ?: Random.nextBytes(ByteArray(serviceDataConfig.byteSize)).also { bytes ->
                sharedPreferences.edit()
                    .putString(KEY_SERVICE_DATA, bytes.stringify())
                    .apply()
            }
    }

    companion object {
        private const val KEY_SERVICE_DATA = "service_data"
    }
}
