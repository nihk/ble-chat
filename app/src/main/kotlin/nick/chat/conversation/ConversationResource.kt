package nick.chat.conversation

import ble.connecting.BluetoothConnector

data class ConversationResource(
    val state: BluetoothConnector.State,
    val items: List<ConversationItem>
)
