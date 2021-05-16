package nick.chat.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
class Device(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val messageIdentifier: ByteArray,
    val address: String,
    val name: String?,
    val lastSeen: Long
)
