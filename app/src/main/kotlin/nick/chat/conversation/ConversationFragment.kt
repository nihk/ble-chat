package nick.chat.conversation

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import ble.usability.ui.BluetoothUsabilitySideEffectHandler
import nick.chat.R
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.chat.chatlist.ServerRepository
import nick.chat.data.bytify
import nick.chat.data.stringify
import nick.chat.databinding.ConversationFragmentBinding
import nick.chat.ui.SnackbarManager

// todo: scroll recyclerview from bottom, like most chat apps UX
class ConversationFragment @Inject constructor(
    private val vmFactory: ConversationViewModel.Factory
) : Fragment(R.layout.conversation_fragment) {
    private val address: String get() = requireArguments().getString(KEY_ADDRESS)!!
    private val identifier: ByteArray get() = requireArguments().getString(KEY_IDENTIFIER)?.bytify()!!
    private val viewModel: ConversationViewModel by viewModels { vmFactory.create(identifier) }
    private lateinit var snackbarManager: SnackbarManager
    private lateinit var usabilitySideEffectHandler: BluetoothUsabilitySideEffectHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        snackbarManager = SnackbarManager()
        usabilitySideEffectHandler = BluetoothUsabilitySideEffectHandler(
            this,
            { justification, prompt, onPrompted ->
                snackbarManager.showSnackbar(requireView(), justification, prompt, onPrompted)
            },
            { viewModel.tryUsingBluetooth() }
        ).also { it.initialize() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ConversationFragmentBinding.bind(view)
        val adapter = ConversationItemAdapter()
        binding.recyclerView.adapter = adapter

        // Hack: this has to be called before viewModel.bluetoothUsability(), otherwise it'll miss
        // any SharedFlow emissions resulting from Bluetooth becoming usable.
        // fixme: use Channel instead: https://old.reddit.com/r/androiddev/comments/nexb2r/migrating_from_livedata_to_kotlins_flow/gykqz3f/
        viewModel.serverEvents
            // No point in advertising while the app is backgrounded.
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { startResult ->
                when (startResult) {
                    is ServerRepository.Event.AdvertisingFailed -> TODO("Show dialog?")
                    is ServerRepository.Event.Disconnected -> {}
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.sideEffects
            // Keep restarting whenever onStart hits, so that the usability is as up to date as can be.
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { sideEffect ->
                snackbarManager.dismiss()
                usabilitySideEffectHandler.onSideEffect(sideEffect)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.items
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
