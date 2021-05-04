package nick.template.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = BluetoothFragmentBinding.bind(view)

        viewModel.states
            // Battery efficiency - don't listen to Bluetooth while in background
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when (state) {
                    is State.NeedsPermissions -> requestPermissions(state.permissions)
                    State.BluetoothIsntOn -> binding.message.text = "BT isn't on -- turn it on!"
                    State.DeniedPermissions -> {
                        // todo: show a button to enable permissions at system level
                        binding.message.text = "You need BT permissions to continue, bro"
                    }
                    State.StartedScanning -> binding.message.text = "Started scanning..."
                    is State.Scanned -> binding.message.text = "Scanned: ${state.result}"
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun requestPermissions(permissions: List<String>) {
        val bundle = PermissionsFragment.bundle(permissions)
        // Ask for permissions in a new Fragment. This means once permissions are granted/denied
        // in that new Fragment, the navigation comes back here and resubscribes to ViewModel states.
        findNavController().navigate(AppNavGraph.Destination.permissions, bundle)
    }
}
