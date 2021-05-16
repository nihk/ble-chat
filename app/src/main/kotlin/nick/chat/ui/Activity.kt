package nick.chat.ui

import android.app.Activity
import dagger.hilt.android.EntryPointAccessors

inline fun <reified T> Activity.entryPoint(): T {
    return EntryPointAccessors.fromActivity(this, T::class.java)
}
