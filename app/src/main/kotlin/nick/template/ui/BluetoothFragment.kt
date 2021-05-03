package nick.template.ui

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.databinding.BluetoothFragmentBinding
import nick.template.navigation.AppNavGraph
import javax.inject.Inject

// todo: tag game - shows a list of nearby devices and you can tag them - i.e. "you're it!" and your
//  status shown as red or blue IT and NOT IT, respectively
class BluetoothFragment @Inject constructor(
    private val vmFactory: BluetoothViewModel.Factory
) : Fragment(R.layout.bluetooth_fragment) {

    private val viewModel: BluetoothViewModel by viewModels { vmFactory.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(PermissionsFragment.RESULT_REQUEST_KEY) { _, bundle ->
            val gotPermissions = bundle.getBoolean(PermissionsFragment.KEY_GOT_PERMISSIONS)
            viewModel.setPermissionsResult(gotPermissions)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = BluetoothFragmentBinding.bind(view)

        viewModel.states
            // Battery efficiency - don't listen to Bluetooth while in background
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                val permissionState = state.permissionsState
                when (permissionState) {
                    PermissionsState.RequestPermissions -> {
                        // todo: use Companion device pairing for O+
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            viewModel.setPermissionsResult(true)
//                        } else {
                            viewModel.onRequestingPermissions()
                            requestPermissions()
//                        }
                    }
                    PermissionsState.RequestingPermissions -> binding.message.text = "Requesting permissions..."
                    PermissionsState.GotPermissions -> binding.message.text = "Got permissions! bluetooth state: ${state.bluetoothState}"
                    PermissionsState.DeniedPermissions -> binding.message.text = "Can't do BLE without permissions :("
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun requestPermissions() {
        val permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val bundle = PermissionsFragment.bundle(permissions)
        findNavController().navigate(AppNavGraph.Destination.permissions, bundle)
    }
}
