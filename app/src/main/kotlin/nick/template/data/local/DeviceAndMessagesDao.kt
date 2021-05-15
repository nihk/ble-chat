package nick.template.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("""
        SELECT *
        FROM devices
        ORDER BY address
    """)
    fun queryAll(): List<Device>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(devices: List<Device>)

    @Query("""
        DELETE
        FROM devices
    """)
    suspend fun nuke()

    @Transaction
    suspend fun insertAndPurgeOldDevices(
        devices: List<Device>,
        threshold: Long
    ) {
        val cachedDevices = queryAll()
            .filter { device -> device.lastSeen >= threshold }
        nuke()
        insert(cachedDevices + devices)
    }

    @Insert
    suspend fun insert(message: Message)
}
