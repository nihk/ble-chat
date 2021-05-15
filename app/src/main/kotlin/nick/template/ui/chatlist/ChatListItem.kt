package nick.template.ui.chatlist

import androidx.room.Embedded
import androidx.room.Relation
import nick.template.data.local.Device
import nick.template.data.local.Message

data class ChatListItem(
    @Embedded
    val device: Device,
    @Relation(
        parentColumn = "address",
        entityColumn = "conversation"
    )
    val messages: List<Message>
)
