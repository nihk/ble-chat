package nick.template.ui

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.databinding.BluetoothFragmentBinding
import javax.inject.Inject

// todo: bluetooth chat interface
// fixme: need to listen to location states, e.g.
//  Settings.Secure.getInt(context.contentResolver, LOCATION_MODE)
//  and use Settings.ACTION_LOCATION_SOURCE_SETTINGS or LocationRequestSettings (Play services)
//  to prompt.
class BluetoothFragment @Inject constructor(
    private val vmFactory: BluetoothViewModel.Factory
) : Fragment(R.layout.bluetooth_fragment) {

    private val viewModel: BluetoothViewModel by viewModels { vmFactory.create(this) }
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.userInteractedWithPermissions()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = BluetoothFragmentBinding.bind(view)

        viewModel.states()
            // Battery efficiency - don't listen to Bluetooth while in background
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when (state) {
                    is State.RequestPermissions -> requestPermissionsLauncher.launch(state.permissions.toTypedArray())
                    // todo: prompt using BluetoothAdapter.ACTION_REQUEST_ENABLE so system takes care of this
                    //  use an ActivityResultContract for this
                    State.BluetoothIsntOn -> binding.message.text = "BT isn't on -- turn it on!"
                    State.DeniedPermissions -> {
                        // todo: show a button to enable permissions at system level
                        binding.message.text = "You need BT permissions to continue, bro"
                    }
                    State.StartedScanning -> binding.message.text = "Started scanning..."
                    // todo: if result was an errorCode, restart. These are not recoverable.
                    is State.Scanned -> binding.message.text = "Scanned: ${state.result}"
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
