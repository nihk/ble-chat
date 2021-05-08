package nick.template.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val conversation: String,
    val from: String,
    val to: String,
    val text: String,
    val timestamp: Long
)
