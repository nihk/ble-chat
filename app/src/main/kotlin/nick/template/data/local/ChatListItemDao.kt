package nick.template.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nick.template.ui.chatlist.ChatListItem

@Dao
interface ChatListItemDao {
    @Transaction
    @Query("""
        SELECT *
        FROM devices
    """)
    fun selectAll(): Flow<List<ChatListItem>>
}
