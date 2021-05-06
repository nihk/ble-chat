package nick.template.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey
    val address: String,
    val name: String?,
    val lastSeen: Long
)
