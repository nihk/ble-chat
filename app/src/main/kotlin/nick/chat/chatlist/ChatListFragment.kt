package nick.chat.chatlist

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.chat.R
import ble.usability.ui.BluetoothUsabilitySideEffectHandler
import nick.chat.data.Resource
import nick.chat.databinding.ChatListFragmentBinding
import nick.chat.ui.SnackbarManager

// todo: move activity result/dialog etc. options to dedicated class that takes in a fragment
// todo: need to clean up messages in database when associated devices are removed
// todo: automated espresso tests for all these states
class ChatListFragment @Inject constructor(
    private val vmFactory: ChatListViewModel.Factory,
    private val openConversationCallback: OpenConversationCallback
) : Fragment(R.layout.chat_list_fragment) {

    private val viewModel: ChatListViewModel by viewModels { vmFactory.create(this) }
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
        val binding = ChatListFragmentBinding.bind(view)
        val adapter = ChatListItemAdapter { item ->
            snackbarManager.dismiss()
            openConversationCallback.with(item)
        }
        binding.recyclerView.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        binding.recyclerView.adapter = adapter
        binding.retry.setOnClickListener { viewModel.tryUsingBluetooth() }
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.tryUsingBluetooth() }

        // Hack: this has to be called before viewModel.bluetoothUsability(), otherwise it'll miss
        // any SharedFlow emissions resulting from Bluetooth becoming usable.
        viewModel.serverEvents()
            // No point in advertising while the app is backgrounded.
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { startResult ->
                when (startResult) {
                    is ServerRepository.Event.AdvertisingFailed -> TODO("Show dialog?")
                    is ServerRepository.Event.Disconnected -> {}
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.bluetoothUsability()
            // Keep restarting whenever onStart hits, so that the usability is as up to date as can be.
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { sideEffect ->
                snackbarManager.dismiss()
                usabilitySideEffectHandler.onSideEffect(sideEffect)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.items()
            .onEach { resource: Resource<List<ChatListItem>> ->
                if (resource !is Resource.Loading) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                // fixme: improve readability here
                binding.topProgressBar.isVisible = resource is Resource.Loading
                    && !resource.data.isNullOrEmpty()
                binding.centerProgressBar.isVisible = resource is Resource.Loading
                    && resource.data.isNullOrEmpty()

                // fixme: don't make this overlap with snackbar when there's an error + empty results
                binding.noResults.isVisible = resource !is Resource.Loading
                    && resource.data.isNullOrEmpty()
                    && adapter.currentList.isEmpty()

                if (!resource.data.isNullOrEmpty()) {
                    adapter.submitList(resource.data)
                }

                if (resource is Resource.Error) {
                    snackbarManager.showSnackbar(
                        view = view,
                        message = resource.throwable.message.toString(),
                        buttonText = "Retry"
                    ) {
                        viewModel.tryUsingBluetooth()
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
