package nick.main

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavActionBuilder
import androidx.navigation.NavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.fragment
import ble.usability.BluetoothUsability
import ble.usability.ui.OpenLocationSettings
import ble.usability.ui.TurnOnBluetooth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.chat.R
import nick.chat.chatlist.ChatListFragment
import nick.chat.conversation.ConversationFragment
import nick.chat.databinding.MainActivityBinding
import nick.chat.di.MainEntryPoint
import nick.chat.navigation.AppNavGraph
import nick.chat.ui.SnackbarManager
import nick.chat.ui.entryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val snackbarManager = SnackbarManager()
    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.tryUsingBluetooth()
    }
    private val turnOnBluetoothLauncher = registerForActivityResult(TurnOnBluetooth()) { didTurnOn ->
        if (!didTurnOn) {
            viewModel.tryUsingBluetooth()
        } // else BluetoothStates will emit an On event, retriggering the ViewModel flow automatically.
    }
    private val turnOnLocationLauncher = registerForActivityResult(OpenLocationSettings()) {
        // User navigating back to app will automatically resubscribe to evaluating Bluetooth usability.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val entryPoint = entryPoint<MainEntryPoint>()
        supportFragmentManager.fragmentFactory = entryPoint.fragmentFactory
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNavGraph(entryPoint.navController)

        viewModel = ViewModelProvider(this, entryPoint.mainViewModelFactory)
            .get(MainViewModel::class.java)

        viewModel.sideEffects
            .flowWithLifecycle(lifecycle)
            .onEach { sideEffect ->
                snackbarManager.dismiss()
                handleSideEffect(sideEffect, binding)
            }
            .launchIn(lifecycleScope)

        viewModel.serverEvents
            // No point in advertising while the app is backgrounded.
            .flowWithLifecycle(lifecycle)
            .onEach { startResult ->
                when (startResult) {
                    is ServerRepository.Event.AdvertisingFailed -> TODO("Show dialog?")
                    is ServerRepository.Event.Disconnected -> {}
                }
            }
            .launchIn(lifecycleScope)

        viewModel.titles()
            .onEach { title ->
                setTitle(title)
            }
            .launchIn(lifecycleScope)
    }

    private fun createNavGraph(navController: NavController) {
        navController.graph = navController.createGraph(
            id = AppNavGraph.id,
            startDestination = AppNavGraph.Destinations.chatList
        ) {
            fragment<ChatListFragment>(AppNavGraph.Destinations.chatList) {
                action(AppNavGraph.Actions.toConversation) {
                    destinationId = AppNavGraph.Destinations.conversation
                    defaultAnimations()
                }
            }
            fragment<ConversationFragment>(AppNavGraph.Destinations.conversation)
        }
    }

    private fun NavActionBuilder.defaultAnimations() {
        navOptions {
            anim {
                enter = R.animator.nav_default_enter_anim
                exit = R.animator.nav_default_exit_anim
                popEnter = R.animator.nav_default_pop_enter_anim
                popExit = R.animator.nav_default_pop_exit_anim
            }
        }
    }

    private fun handleSideEffect(
        sideEffect: BluetoothUsability.SideEffect,
        binding: MainActivityBinding
    ) {
        when (sideEffect) {
            is BluetoothUsability.SideEffect.RequestPermissions -> {
                if (sideEffect.numTimesRequestedPreviously == 0) {
                    requestPermissionsLauncher.launch(sideEffect.permissions.toTypedArray())
                } else {
                    snackbarManager.showSnackbar(
                        view = binding.root,
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
                    snackbarManager.showSnackbar(
                        view = binding.root,
                        message = "You need BT to use this app",
                        buttonText = "Turn on"
                    ) {
                        turnOnBluetoothLauncher.launch(Unit)
                    }
                }
            }
            is BluetoothUsability.SideEffect.AskToTurnLocationOn -> {
                if (sideEffect.numTimesAskedPreviously == 0) {
                    AlertDialog.Builder(this)
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
                } else {
                    snackbarManager.showSnackbar(
                        view = binding.root,
                        message = "You need Location on to use this app",
                        buttonText = "Turn on"
                    ) {
                        turnOnLocationLauncher.launch(Unit)
                    }
                }
            }
        }
    }
}
