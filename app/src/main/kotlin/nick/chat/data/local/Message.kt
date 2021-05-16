package nick.chat.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val conversation: ByteArray,
    val isMe: Boolean,
    val text: String,
    val timestamp: Long
)
