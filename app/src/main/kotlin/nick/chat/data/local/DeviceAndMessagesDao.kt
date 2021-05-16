package nick.chat.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceAndMessagesDao {
    @Transaction
    @Query("""
        SELECT *
        FROM devices
    """)
    fun selectAll(): Flow<List<DeviceAndMessages>>
}
