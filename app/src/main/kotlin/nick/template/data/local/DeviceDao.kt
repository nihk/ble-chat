package nick.template.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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
        val cachedDevices = selectAll().first()
            .filter { device -> device.lastSeen >= threshold }
        nuke()
        insert(cachedDevices + devices)
    }
}
