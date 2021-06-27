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
    address: String,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    val items = conversationRepository.items(conversation)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )
        .filterNotNull()

    val connection = conversationRepository.connectTo(conversation, address)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )
        .filterNotNull()

    fun send(input: String) {
        conversationRepository.send(input)
    }

    class Factory @Inject constructor(
        private val conversationRepository: ConversationRepository
    ) {
        fun create(
            conversation: ByteArray,
            address: String
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ConversationViewModel(
                        conversation,
                        address,
                        conversationRepository
                    ) as T
                }
            }
        }
    }
}
