package nick.chat.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class DeviceAndMessages(
    @Embedded
    val device: Device,
    @Relation(
        parentColumn = "identifier",
        entityColumn = "conversation"
    )
    val messages: List<Message>
)
