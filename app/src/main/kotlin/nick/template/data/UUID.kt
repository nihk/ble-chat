package nick.template.data

import java.nio.ByteBuffer
import java.util.UUID

fun UUID.toByteArray(bytes: Int = 16): ByteArray {
    val buffer = ByteBuffer.wrap(ByteArray(bytes))
    buffer.putLong(mostSignificantBits)
    return buffer.array()
}
