package nick.chat.chatlist

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.chat.R
import nick.chat.data.Resource
import ble.usability.BluetoothUsability
import nick.chat.databinding.ChatListFragmentBinding
import nick.chat.ui.OpenLocationSettings
import nick.chat.ui.TurnOnBluetooth

// todo: move activity result/dialog etc. options to dedicated class that takes in a fragment
// todo: need to clean up messages in database when associated devices are removed
// todo: automated espresso tests for all these states
class ChatListFragment @Inject constructor(
    private val vmFactory: ChatListViewModel.Factory,
    private val openConversationCallback: OpenConversationCallback
) : Fragment(R.layout.chat_list_fragment) {

    private val viewModel: ChatListViewModel by viewModels { vmFactory.create(this) }
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var turnOnBluetoothLauncher: ActivityResultLauncher<Unit>
    private lateinit var turnOnLocationLauncher: ActivityResultLauncher<Unit>
    private var errorMessage: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.tryUsingBluetooth()
        }
        turnOnBluetoothLauncher = registerForActivityResult(TurnOnBluetooth()) { didTurnOn ->
            if (!didTurnOn) {
                viewModel.tryUsingBluetooth()
            } // else BluetoothStates will emit an On event, retriggering the ViewModel flow automatically.
        }
        turnOnLocationLauncher = registerForActivityResult(OpenLocationSettings()) {
            // User navigating back to app will automatically resubscribe to evaluating Bluetooth usability.
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ChatListFragmentBinding.bind(view)
        val adapter = ChatListItemAdapter { item ->
            cleanUpAnyStaleState()
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
                cleanUpAnyStaleState()
                when (sideEffect) {
                    is BluetoothUsability.SideEffect.RequestPermissions -> {
                        if (sideEffect.numTimesRequestedPreviously == 0) {
                            requestPermissionsLauncher.launch(sideEffect.permissions.toTypedArray())
                        } else {
                            showSnackbar(
                                view = view,
                                message = "You need BT permissions to continue, bro",
                                buttonText = "Grant"
                            ) {
                                requestPermissionsLauncher.launch(sideEffect.permissions.toTypedArray())
                            }
                        }
                    }
                    // todo: what about other BluetoothAdapter actions, e.g. ACTION_REQUEST_DISCOVERABLE?
                    is BluetoothUsability.SideEffect.AskToTurnBluetoothOn -> {
                        if (sideEffect.numTimesAskedPreviously == 0) {
                            turnOnBluetoothLauncher.launch(Unit)
                        } else {
                            showSnackbar(
                                view = view,
                                message = "You need BT to use this app",
                                buttonText = "Turn on"
                            ) {
                                turnOnBluetoothLauncher.launch(Unit)
                            }
                        }
                    }
                    is BluetoothUsability.SideEffect.AskToTurnLocationOn -> {
                        if (sideEffect.numTimesAskedPreviously == 0) {
                            showTurnOnLocationDialog()
                        } else {
                            showSnackbar(
                                view = view,
                                message = "You need Location on to use this app",
                                buttonText = "Turn on"
                            ) {
                                turnOnLocationLauncher.launch(Unit)
                            }
                        }
                    }
                }
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
                    showSnackbar(
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

    private fun showSnackbar(
        view: View,
        message: String,
        buttonText: String? = null,
        action: (View) -> Unit = {}
    ) {
        dismissSnackbar()
        errorMessage = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(buttonText, action)
            .also { it.show() }
    }

    private fun dismissSnackbar() {
        errorMessage?.dismiss()
        errorMessage = null
    }

    private fun cleanUpAnyStaleState() {
        dismissSnackbar()
    }

    private fun showTurnOnLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Turn on Location")
            .setMessage("Location needs to be turned on to use Bluetooth. Weird, I know.")
            .setPositiveButton("Open settings") { _, _ ->
                turnOnLocationLauncher.launch(Unit)
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewModel.tryUsingBluetooth()
            }
            .setOnCancelListener {
                viewModel.tryUsingBluetooth()
            }
            .create()
            .show()
    }
}
