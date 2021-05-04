package nick.template.data

import kotlinx.coroutines.channels.SendChannel

fun <E> SendChannel<E>.offerSafely(element: E): Boolean {
    return !isClosedForSend && offer(element)
}
