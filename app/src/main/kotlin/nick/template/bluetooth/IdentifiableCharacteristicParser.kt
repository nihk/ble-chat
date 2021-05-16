package nick.template.bluetooth

import ble.CharacteristicParser
import ble.ServiceDataConfig
import ble.serving.BluetoothServer
import javax.inject.Inject

class IdentifiableCharacteristicParser @Inject constructor(
    private val serviceDataConfig: ServiceDataConfig
) : CharacteristicParser {
    override fun parse(value: ByteArray): BluetoothServer.Event.Message {
        val identifierPart = value.copyOfRange(0, serviceDataConfig.byteSize)
        val messagePart = value.copyOfRange(serviceDataConfig.byteSize, value.size)
            .toString(Charsets.UTF_8)

        return BluetoothServer.Event.Message(
            identifier = identifierPart,
            message = messagePart
        )
    }
}
