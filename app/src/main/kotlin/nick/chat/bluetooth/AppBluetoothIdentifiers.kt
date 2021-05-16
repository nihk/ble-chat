package nick.chat.bluetooth

import android.content.SharedPreferences
import ble.BluetoothIdentifiers
import ble.ServiceDataConfig
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class AppBluetoothIdentifiers @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val serviceDataConfig: ServiceDataConfig
) : BluetoothIdentifiers {
    override val service: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
    override val message: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
    override val serviceData: ByteArray = run {
        // fixme: need to test this encoding/decoding works as i want it to
        sharedPreferences.getString(KEY_SERVICE_DATA, null)?.toByteArray(Charsets.ISO_8859_1)
            ?: Random.nextBytes(ByteArray(serviceDataConfig.byteSize)).also { bytes ->
                sharedPreferences.edit()
                    .putString(KEY_SERVICE_DATA, String(bytes, Charsets.ISO_8859_1))
                    .apply()
            }
    }

    companion object {
        private const val KEY_SERVICE_DATA = "service_data"
    }
}
