package nick.template.data.bluetooth.usability

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface BluetoothPermissions {
    fun state(): State

    sealed class State {
        data class MissingPermissions(val permissions: List<String>) : State()
        object HasPermissions : State()
    }
}

class AndroidBluetoothPermissions @Inject constructor(
    @ApplicationContext private val context: Context
) : BluetoothPermissions {

    override fun state(): BluetoothPermissions.State {
        val hasPermissions = REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        return if (hasPermissions) {
            BluetoothPermissions.State.HasPermissions
        } else {
            BluetoothPermissions.State.MissingPermissions(REQUIRED_PERMISSIONS)
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
