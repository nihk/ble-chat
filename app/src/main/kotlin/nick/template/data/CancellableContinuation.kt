package nick.template.data

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

fun <T> CancellableContinuation<T>.resumeSafely(value: T) {
    if (isCompleted) return
    resume(value)
}
