package nick.template.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MessageDao {
    @Query(
        """
        SELECT *
        FROM messages
        WHERE conversation = :conversation
        ORDER BY timestamp
    """
    )
    fun selectAll(conversation: String): List<Message>
}
