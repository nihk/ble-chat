package nick.template.data

import kotlinx.coroutines.channels.SendChannel

// https://github.com/Kotlin/kotlinx.coroutines/issues/974
fun <E> SendChannel<E>.offerSafely(element: E) {
    runCatching {
        offer(element)
    }
}
