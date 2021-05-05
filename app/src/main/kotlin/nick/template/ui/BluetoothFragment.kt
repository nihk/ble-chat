package nick.template.ui

import android.os.Bundle
import android.util.Log
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
//  check out how other repositories are handling things like:
//  * storage of historical messages
//  * connecting to known devices
// fixme: need to listen to location states, e.g.
//  Settings.Secure.getInt(context.contentResolver, LOCATION_MODE)
//  and use Settings.ACTION_LOCATION_SOURCE_SETTINGS or LocationRequestSettings (Play services)
//  to prompt.
class BluetoothFragment @Inject constructor(
    private val vmFactory: BluetoothViewModel.Factory
) : Fragment(R.layout.bluetooth_fragment) {

    private val viewModel: BluetoothViewModel by viewModels { vmFactory.create(this) }
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var turnOnBluetoothLauncher: ActivityResultLauncher<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val grantedAll = permissions.isNotEmpty() && permissions.all { it.value }
            if (grantedAll) {
                viewModel.userGrantedPermissions()
            } else {
                viewModel.userDeniedPermissions()
            }
        }
        turnOnBluetoothLauncher = registerForActivityResult(TurnOnBluetooth()) { didTurnOn ->
            if (!didTurnOn) {
                viewModel.userDeniedTurningBluetoothOn()
            } // else BluetoothStates will emit an On event, retriggering the ViewModel flow.
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = BluetoothFragmentBinding.bind(view)

        viewModel.states()
            // Battery efficiency - don't listen to Bluetooth while in background
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                Log.d("asdf", state.toString())
                when (state) {
                    is State.RequestPermissions -> requestPermissionsLauncher.launch(state.permissions.toTypedArray())
                    // todo: what about other BluetoothAdapter actions, e.g. ACTION_REQUEST_DISCOVERABLE?
                    State.AskToTurnBluetoothOn -> turnOnBluetoothLauncher.launch(Unit)
                    State.DeniedEnablingBluetooth -> {
                        // todo: show a button to enable bluetooth
                        binding.message.text = "You need BT to use this app"
                    }
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
