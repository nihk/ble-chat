package nick.template.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import nick.template.data.Resource
import nick.template.data.bluetooth.usability.BluetoothUsability
import nick.template.data.bluetooth.ConversationRepository
import nick.template.data.local.Message
import javax.inject.Inject

class ChatViewModel(
    private val bluetoothUsability: BluetoothUsability,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val messages = MutableStateFlow<Resource<List<Message>>?>(null)
    fun messages(): Flow<Resource<List<Message>>> = messages.filterNotNull()

    fun bluetoothUsability(): Flow<BluetoothUsability.SideEffect> = bluetoothUsability.sideEffects()

    class Factory @Inject constructor(
        private val bluetoothUsability: BluetoothUsability,
        private val conversationRepository: ConversationRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(bluetoothUsability, conversationRepository) as T
        }
    }
}
