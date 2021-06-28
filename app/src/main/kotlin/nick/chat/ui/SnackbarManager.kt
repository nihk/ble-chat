package nick.chat.ui

import android.view.View
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class SnackbarManager @Inject constructor()  {
    private var snackbar: Snackbar? = null

    fun showSnackbar(
        view: View,
        message: String,
        buttonText: String? = null,
        action: () -> Unit = {}
    ) {
        dismiss()
        val duration = if (buttonText == null) Snackbar.LENGTH_SHORT else Snackbar.LENGTH_INDEFINITE
        snackbar = Snackbar.make(view, message, duration)
            .setAction(buttonText) { action() }
            .also { it.show() }
    }

    fun dismiss() {
        snackbar?.dismiss()
        snackbar = null
    }
}
