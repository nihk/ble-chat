package nick.template.ui

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.data.bluetooth.BluetoothUsability
import nick.template.data.bluetooth.DevicesResource
import nick.template.databinding.DevicesFragmentBinding
import nick.template.ui.adapters.DeviceAdapter
import javax.inject.Inject

// todo: bluetooth chat interface
//  check out how other repositories are handling things like:
//  * storage of historical messages
//  * connecting to known devices
// fixme: need to listen to location states, e.g.
//  Settings.Secure.getInt(context.contentResolver, LOCATION_MODE)
//  and use Settings.ACTION_LOCATION_SOURCE_SETTINGS or LocationRequestSettings (Play services)
//  to prompt.
// todo: add refresh menu button
class DevicesFragment @Inject constructor(
    private val vmFactory: BluetoothViewModel.Factory,
    private val openChatCallback: OpenChatCallback
) : Fragment(R.layout.devices_fragment) {

    private val viewModel: BluetoothViewModel by viewModels { vmFactory.create(this) }
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var turnOnBluetoothLauncher: ActivityResultLauncher<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val grantedAll = permissions.isNotEmpty() && permissions.all { it.value }
            if (grantedAll) {
                viewModel.promptIfNeeded()
            } else {
                viewModel.denyPermissions()
            }
        }
        turnOnBluetoothLauncher = registerForActivityResult(TurnOnBluetooth()) { didTurnOn ->
            if (!didTurnOn) {
                viewModel.denyTurningBluetoothOn()
            } // else BluetoothStates will emit an On event, retriggering the ViewModel flow automatically.
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = DevicesFragmentBinding.bind(view)
        val adapter = DeviceAdapter(openChatCallback)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        binding.recyclerView.adapter = adapter

        viewModel.bluetoothUsability()
            // Keep restarting whenever onStart hits, so that the usability is as up to date as can be.
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { sideEffect ->
                when (sideEffect) {
                    is BluetoothUsability.SideEffect.RequestPermissions -> requestPermissionsLauncher.launch(sideEffect.permissions.toTypedArray())
                    // todo: what about other BluetoothAdapter actions, e.g. ACTION_REQUEST_DISCOVERABLE?
                    BluetoothUsability.SideEffect.AskToTurnBluetoothOn -> turnOnBluetoothLauncher.launch(Unit)
                    BluetoothUsability.SideEffect.InformBluetoothRequired -> {
                        showSnackbar(
                            view = view,
                            message = "You need BT to use this app",
                            buttonText = "Turn on"
                        ) {
                            viewModel.promptIfNeeded()
                        }
                    }
                    BluetoothUsability.SideEffect.InformPermissionsRequired -> {
                        // todo: deep link to settings permissions?
                        showSnackbar(
                            view = view,
                            message = "You need BT permissions to continue, bro",
                            buttonText = "Grant"
                        ) {
                            viewModel.promptIfNeeded()
                        }
                    }
                    BluetoothUsability.SideEffect.UseBluetooth -> viewModel.scanForDevices()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.devices()
            .onEach { resource ->
                binding.topProgressBar.isVisible = resource is DevicesResource.Loading
                    && !resource.devices.isNullOrEmpty()
                binding.centerProgressBar.isVisible = resource is DevicesResource.Loading
                    && resource.devices.isNullOrEmpty()

                if (!resource.devices.isNullOrEmpty()) {
                    adapter.submitList(resource.devices)
                }

                if (resource is DevicesResource.Error) {
                    showSnackbar(
                        view = view,
                        message = "Error: ${resource.errorCode}",
                        buttonText = "Retry"
                    ) {
                        // These are not recoverable.
                        viewModel.promptIfNeeded()
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
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(buttonText, action)
            .show()
    }
}
