package nick.template.ui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class PermissionsFragment : Fragment() {

    private val permissions get() = requireArguments().getStringArray(KEY_PERMISSIONS_ARG)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasPermissions()) {
            setResult(true)
        } else {
            lifecycleScope.launch {
                val result = activityResult(
                    permissions,
                    ActivityResultContracts.RequestMultiplePermissions()
                )
                setResult(gotPermissions = result.all { it.value })
            }
        }
    }

    private fun setResult(gotPermissions: Boolean) {
        setFragmentResult(RESULT_REQUEST_KEY, bundleOf(KEY_GOT_PERMISSIONS to gotPermissions))
        findNavController().popBackStack()
    }

    private fun hasPermissions(): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        const val RESULT_REQUEST_KEY = "permissions_result_request_key"
        const val KEY_GOT_PERMISSIONS = "got_permissions"
        private const val KEY_PERMISSIONS_ARG = "permissions_arg"

        fun bundle(permissions: List<String>): Bundle {
            return Bundle().apply {
                putStringArray(KEY_PERMISSIONS_ARG, permissions.toTypedArray())
            }
        }
    }
}
