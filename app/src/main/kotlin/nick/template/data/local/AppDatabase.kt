package nick.template.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Device::class, Message::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun messageDao(): MessageDao
}
