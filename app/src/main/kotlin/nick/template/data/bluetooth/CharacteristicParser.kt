package nick.template.data.bluetooth

import javax.inject.Inject
import nick.template.data.bluetooth.serving.BluetoothServer

interface CharacteristicParser {
    fun parse(value: ByteArray): BluetoothServer.Event.Message
}

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
