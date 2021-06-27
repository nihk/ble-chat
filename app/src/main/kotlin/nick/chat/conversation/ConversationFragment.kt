package nick.chat.conversation

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import ble.connecting.BluetoothConnector
import ble.usability.BluetoothUsability
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.chat.R
import nick.chat.data.bytify
import nick.chat.data.stringify
import nick.chat.databinding.ConversationFragmentBinding
import nick.chat.ui.SnackbarManager
import nick.main.MainViewModel

// todo: scroll recyclerview from bottom, like most chat apps UX
class ConversationFragment @Inject constructor(
    private val conversationVmFactory: ConversationViewModel.Factory,
    private val mainViewModelFactory: MainViewModel.Factory,
    private val snackbarManager: SnackbarManager
) : Fragment(R.layout.conversation_fragment) {
    private val name: String? get() = requireArguments().getString(KEY_NAME)
    private val address: String get() = requireArguments().getString(KEY_ADDRESS)!!
    private val identifier: ByteArray get() = requireArguments().getString(KEY_IDENTIFIER)?.bytify()!!
    private val conversationViewModel: ConversationViewModel by viewModels { conversationVmFactory.create(identifier, address) }
    private val mainViewModel: MainViewModel by activityViewModels { mainViewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ConversationFragmentBinding.bind(view)
        val adapter = ConversationItemAdapter()
        binding.recyclerView.adapter = adapter
        bind(binding)

        mainViewModel.setTitle(name ?: address)

        // fixme: use refresh-style template that ChatListFragment/ViewModel does
        mainViewModel.sideEffects
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .filter { it is BluetoothUsability.SideEffect.UseBluetooth }
            .flatMapLatest {
                combine(
                    conversationViewModel.items,
                    conversationViewModel.connection
                ) { items, state ->
                    Pair(items, state)
                }
            }
            .onEach { pair ->
                val items = pair.first
                val state = pair.second
                if (!items.isNullOrEmpty()) {
                    adapter.submitList(items)
                }

                binding.input.isEnabled = state !is BluetoothConnector.State.Error
                    && state !is BluetoothConnector.State.Initial

                when (state) {
                    is BluetoothConnector.State.Error -> snackbarManager.showSnackbar(view, "Error: ${state.status}")
                }

                binding.topProgressBar.isVisible = state is BluetoothConnector.State.Initial
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun bind(binding: ConversationFragmentBinding) {
        binding.send.setOnClickListener {
            val input = binding.input.text.toString()
            if (input.isBlank()) return@setOnClickListener
            binding.input.text.clear()
            conversationViewModel.send(input)
        }
    }

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_ADDRESS = "address"
        private const val KEY_IDENTIFIER = "identifier"

        fun bundle(name: String?, address: String, identifier: ByteArray): Bundle {
            return bundleOf(
                KEY_NAME to name,
                KEY_ADDRESS to address,
                KEY_IDENTIFIER to identifier.stringify()
            )
        }
    }
}
