package ble

import kotlinx.coroutines.channels.SendChannel

// https://github.com/Kotlin/kotlinx.coroutines/issues/974
internal fun <E> SendChannel<E>.offerSafely(element: E) {
    // todo: might not need this runCatching since kotlin 1.5.0
    runCatching {
        trySend(element)
    }
}
