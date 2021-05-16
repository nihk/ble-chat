package nick.chat.data

import javax.inject.Inject

interface CurrentTime {
    fun millis(): Long
}

class SystemCurrentTime @Inject constructor() : CurrentTime {
    override fun millis(): Long = System.currentTimeMillis()
}
