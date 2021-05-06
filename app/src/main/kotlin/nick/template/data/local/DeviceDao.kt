package nick.template.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("""
        SELECT *
        FROM devices
        ORDER BY address
    """)
    fun selectAll(): Flow<List<Device>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(devices: List<Device>)

    @Transaction
    suspend fun insertAndPurgeOldDevices(devices: List<Device>) {
        // todo: clear out old entries and update existing ones
        insert(devices)
    }
}
