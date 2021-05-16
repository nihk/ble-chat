package nick.chat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Device::class, Message::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun devicesDao(): DevicesDao
    abstract fun messagesDao(): MessagesDao
    abstract fun deviceAndMessagesDao(): DeviceAndMessagesDao
}
