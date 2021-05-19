package nick.chat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {
    @Query("""
        SELECT *
        FROM messages
        WHERE conversation = :conversation
    """)
    fun selectAllByConversation(conversation: ByteArray): Flow<List<Message>>

    @Insert
    suspend fun insert(message: Message)
}
