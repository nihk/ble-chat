package nick.template.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class DeviceAndMessages(
    @Embedded
    val device: Device,
    @Relation(
        parentColumn = "address",
        entityColumn = "conversation"
    )
    val messages: List<Message>
)
