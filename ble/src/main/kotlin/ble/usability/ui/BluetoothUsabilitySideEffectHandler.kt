package ble.usability.ui

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import ble.usability.BluetoothUsability

class BluetoothUsabilitySideEffectHandler(
    private val fragment: Fragment,
    private val justifyRequest: (justification: String, prompt: String, onPrompted: () -> Unit) -> Unit,
    private val tryUsingBluetooth: () -> Unit
) {
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var turnOnBluetoothLauncher: ActivityResultLauncher<Unit>
    private lateinit var turnOnLocationLauncher: ActivityResultLauncher<Unit>

    // Must be called from Fragment.onCreate()
    fun initialize() {
        requestPermissionsLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            tryUsingBluetooth()
        }
        turnOnBluetoothLauncher = fragment.registerForActivityResult(TurnOnBluetooth()) { didTurnOn ->
            if (!didTurnOn) {
                tryUsingBluetooth()
            } // else BluetoothStates will emit an On event, retriggering the ViewModel flow automatically.
        }
        turnOnLocationLauncher = fragment.registerForActivityResult(OpenLocationSettings()) {
            // User navigating back to app will automatically resubscribe to evaluating Bluetooth usability.
        }
    }

    fun onSideEffect(sideEffect: BluetoothUsability.SideEffect) {
        when (sideEffect) {
            is BluetoothUsability.SideEffect.RequestPermissions -> {
                if (sideEffect.numTimesRequestedPreviously == 0) {
                    requestPermissionsLauncher.launch(sideEffect.permissions.toTypedArray())
                } else {
                    justifyRequest(
                        "You need BT permissions to continue, bro",
                        "Grant"
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
                    justifyRequest(
                        "You need BT to use this app",
                        "Turn on"
                    ) {
                        turnOnBluetoothLauncher.launch(Unit)
                    }
                }
            }
            is BluetoothUsability.SideEffect.AskToTurnLocationOn -> {
                if (sideEffect.numTimesAskedPreviously == 0) {
                    showTurnOnLocationDialog()
                } else {
                    justifyRequest(
                        "You need Location on to use this app",
                        "Turn on"
                    ) {
                        turnOnLocationLauncher.launch(Unit)
                    }
                }
            }
        }
    }

    private fun showTurnOnLocationDialog() {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("Turn on Location")
            .setMessage("Location needs to be turned on to use Bluetooth. Weird, I know.")
            .setPositiveButton("Open settings") { _, _ ->
                turnOnLocationLauncher.launch(Unit)
            }
            .setNegativeButton("Cancel") { _, _ ->
                tryUsingBluetooth()
            }
            .setOnCancelListener {
                tryUsingBluetooth()
            }
            .create()
            .show()
    }
}
