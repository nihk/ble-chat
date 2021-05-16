package nick.chat.conversation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import nick.chat.R
import javax.inject.Inject
import nick.chat.data.bytify
import nick.chat.data.stringify

// todo: set a max character limit on edit text - 16 characters
class ConversationFragment @Inject constructor(
    private val vmFactory: ConversationViewModel.Factory
) : Fragment(R.layout.conversation_fragment) {
    // todo: need to pass in identifiable ByteArray
    private val address: String get() = requireArguments().getString(KEY_ADDRESS)!!
    private val identifier: ByteArray get() = requireArguments().getString(KEY_IDENTIFIER)?.bytify()!!
    private val viewModel: ConversationViewModel by viewModels { vmFactory }

    companion object {
        private const val KEY_ADDRESS = "address"
        private const val KEY_IDENTIFIER = "identifier"

        fun bundle(address: String, identifier: ByteArray): Bundle {
            return bundleOf(
                KEY_ADDRESS to address,
                KEY_IDENTIFIER to identifier.stringify()
            )
        }
    }
}
