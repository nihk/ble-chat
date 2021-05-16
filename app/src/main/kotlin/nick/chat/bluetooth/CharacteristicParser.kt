package nick.chat.bluetooth

import ble.ServiceDataConfig
import javax.inject.Inject

class CharacteristicParser @Inject constructor(
    private val serviceDataConfig: ServiceDataConfig
) {
    fun parse(value: ByteArray): Pair<ByteArray, String> {
        val identifierPart = value.copyOfRange(0, serviceDataConfig.byteSize)
        val messagePart = value.copyOfRange(serviceDataConfig.byteSize, value.size)
            .toString(Charsets.UTF_8)

        return Pair(identifierPart, messagePart)
    }
}
