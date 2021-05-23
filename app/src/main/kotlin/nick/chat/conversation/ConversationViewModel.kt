package nick.chat.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn

class ConversationViewModel(
    conversation: ByteArray,
    conversationRepository: ConversationRepository
) : ViewModel() {

    val items = conversationRepository.items(conversation)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )
        .filterNotNull()

    class Factory @Inject constructor(
        private val conversationRepository: ConversationRepository
    ) {
        fun create(conversation: ByteArray): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ConversationViewModel(
                        conversation,
                        conversationRepository
                    ) as T
                }
            }
        }
    }
}
