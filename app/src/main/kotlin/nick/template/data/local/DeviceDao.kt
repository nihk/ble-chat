package nick.template.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("""
        SELECT *
        FROM devices
    """)
    fun selectAll(): Flow<Device>
}
