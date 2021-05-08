package nick.template.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface LocationStates {
    fun states(): Flow<LocationState>
}

class AndroidLocationState(
    @ApplicationContext private val context: Context
) : LocationStates {
    override fun states(): Flow<LocationState> {
        val locationStates = callbackFlow<LocationStates> {
            TODO("listen to location turning off/on")
        }

        TODO()
    }
}
