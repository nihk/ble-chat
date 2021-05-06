package nick.template.ui

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import nick.template.R
import javax.inject.Inject

class ChatFragment @Inject constructor() : Fragment(R.layout.chat_fragment) {
    private val address: String get() = requireArguments().getString(KEY_ADDRESS)!!

    companion object {
        private const val KEY_ADDRESS = "address"

        fun bundle(address: String): Bundle {
            return bundleOf(KEY_ADDRESS to address)
        }
    }
}
