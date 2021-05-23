package nick.chat.conversation

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import ble.usability.BluetoothUsability
import javax.inject.Inject
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.chat.R
import nick.chat.data.bytify
import nick.chat.data.stringify
import nick.chat.databinding.ConversationFragmentBinding
import nick.chat.ui.MainViewModel

// todo: scroll recyclerview from bottom, like most chat apps UX
class ConversationFragment @Inject constructor(
    private val conversationVmFactory: ConversationViewModel.Factory,
    private val mainViewModelFactory: MainViewModel.Factory
) : Fragment(R.layout.conversation_fragment) {
    private val address: String get() = requireArguments().getString(KEY_ADDRESS)!!
    private val identifier: ByteArray get() = requireArguments().getString(KEY_IDENTIFIER)?.bytify()!!
    private val conversationViewModel: ConversationViewModel by viewModels { conversationVmFactory.create(identifier) }
    private val mainViewModel: MainViewModel by activityViewModels { mainViewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ConversationFragmentBinding.bind(view)
        val adapter = ConversationItemAdapter()
        binding.recyclerView.adapter = adapter

        mainViewModel.sideEffects
            .filter { it is BluetoothUsability.SideEffect.UseBluetooth }
            .flatMapLatest { conversationViewModel.items }
            .onEach { items ->
                if (!items.data.isNullOrEmpty()) {
                    adapter.submitList(items.data)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    companion object {
        private const val KEY_ADDRESS = "address"
        private const val KEY_IDENTIFIER = "identifier"

        fun bundle(address: String, identifier: ByteArray): Bundle {
            return bundleOf(
                KEY_ADDRESS to address,
                KEY_IDENTIFIER to identifier.stringify()
            )
        }
    }
}
