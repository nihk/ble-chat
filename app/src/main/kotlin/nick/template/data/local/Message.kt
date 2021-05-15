package nick.template.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val conversation: String,
    val isMe: Boolean,
    val text: String,
    val timestamp: Long
)
