package nick.template.ui.chat

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import nick.template.R
import javax.inject.Inject

class ChatFragment @Inject constructor(
    private val vmFactory: ChatViewModel.Factory
) : Fragment(R.layout.chat_fragment) {
    private val address: String get() = requireArguments().getString(KEY_ADDRESS)!!
    private val viewModel: ChatViewModel by viewModels { vmFactory }

    companion object {
        private const val KEY_ADDRESS = "address"

        fun bundle(address: String): Bundle {
            return bundleOf(KEY_ADDRESS to address)
        }
    }
}
