package nick.template.data.local

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: Message)
}
