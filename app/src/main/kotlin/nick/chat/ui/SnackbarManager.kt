package nick.chat.ui

import android.view.View
import com.google.android.material.snackbar.Snackbar

class SnackbarManager {
    private var snackbar: Snackbar? = null

    fun showSnackbar(
        view: View,
        message: String,
        buttonText: String? = null,
        action: () -> Unit = {}
    ) {
        dismiss()
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(buttonText) { action() }
            .also { it.show() }
    }

    fun dismiss() {
        snackbar?.dismiss()
        snackbar = null
    }
}
