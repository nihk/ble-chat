package nick.template.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface BluetoothPermissions {
    fun state(): State

    sealed class State {
        data class NeedsPermissions(val permissions: List<String>) : State()
        object HasPermissions : State()
    }
}

class AndroidBluetoothPermissions @Inject constructor(
    @ApplicationContext private val context: Context
) : BluetoothPermissions {

    override fun state(): BluetoothPermissions.State {
        val hasPermissions = requiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        return if (hasPermissions) {
            BluetoothPermissions.State.HasPermissions
        } else {
            BluetoothPermissions.State.NeedsPermissions(requiredPermissions())
        }
    }

    private fun requiredPermissions(): List<String> {
        return mutableListOf<String>().apply {
            // fixme: wrap in < Android.O check
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
