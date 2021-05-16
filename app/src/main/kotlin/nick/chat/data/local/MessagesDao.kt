package nick.chat.data.local

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface MessagesDao {
    @Insert
    suspend fun insert(message: Message)
}
