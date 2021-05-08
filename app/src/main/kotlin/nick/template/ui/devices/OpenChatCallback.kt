package nick.template.ui.devices

import androidx.navigation.NavController
import nick.template.data.local.Device
import nick.template.navigation.AppNavGraph
import nick.template.ui.chat.ChatFragment
import javax.inject.Inject

interface OpenChatCallback {
    fun with(device: Device)
}

class DefaultOpenChatCallback @Inject constructor(
    private val navController: NavController
) : OpenChatCallback {
    override fun with(device: Device) {
        navController.navigate(AppNavGraph.Actions.toChat, ChatFragment.bundle(device.address))
    }
}
