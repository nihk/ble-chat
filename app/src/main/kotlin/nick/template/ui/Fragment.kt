package nick.template.ui

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun <I, O> Fragment.activityResult(
    input: I,
    contract: ActivityResultContract<I, O>
) = suspendCancellableCoroutine<O> { continuation ->
    var resultLauncher: ActivityResultLauncher<I>? = null
    val callback = ActivityResultCallback<O> { output ->
        resultLauncher?.unregister()
        continuation.resume(output)
    }

    resultLauncher = registerForActivityResult(contract, callback)
    resultLauncher.launch(input)

    continuation.invokeOnCancellation { resultLauncher.unregister() }
}
