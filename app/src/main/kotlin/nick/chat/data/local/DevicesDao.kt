package nick.chat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DevicesDao {
    @Query("""
        SELECT *
        FROM devices
        ORDER BY address
    """)
    fun selectAll(): List<Device>

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
        val cachedDevices = selectAll()
            .filter { device -> device.lastSeen >= threshold }
        nuke()
        insert(cachedDevices + devices)
    }
}
