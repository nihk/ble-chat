package nick.chat.chatlist

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import ble.usability.BluetoothUsability
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.chat.R
import nick.chat.data.Resource
import nick.chat.databinding.ChatListFragmentBinding
import nick.chat.ui.SnackbarManager
import nick.main.MainViewModel

// todo: need to clean up messages in database when associated devices are removed
// todo: automated espresso tests for all these states
class ChatListFragment @Inject constructor(
    private val chatListVmFactory: ChatListViewModel.Factory,
    private val mainViewModelFactory: MainViewModel.Factory,
    private val openConversationCallback: OpenConversationCallback,
    private val snackbarManager: SnackbarManager
) : Fragment(R.layout.chat_list_fragment) {

    private val chatListViewModel: ChatListViewModel by viewModels { chatListVmFactory.create(this) }
    private val mainViewModel: MainViewModel by activityViewModels { mainViewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ChatListFragmentBinding.bind(view)
        val adapter = ChatListItemAdapter { item -> openConversationCallback.with(item) }
        binding.recyclerView.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        binding.recyclerView.adapter = adapter
        binding.retry.setOnClickListener { chatListViewModel.refresh() }
        binding.swipeRefreshLayout.setOnRefreshListener { chatListViewModel.refresh() }

        mainViewModel.sideEffects
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { sideEffect ->
                val canUseBluetooth = sideEffect == BluetoothUsability.SideEffect.UseBluetooth
                chatListViewModel.setCanUseBluetooth(canUseBluetooth)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        chatListViewModel.items.onEach { resource: Resource<List<ChatListItem>> ->
            snackbarManager.dismiss()
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
                    chatListViewModel.refresh()
                }
            }
        }
        .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
