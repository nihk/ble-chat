package nick.chat.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ByteArrayTest {
    @Test
    fun `back and forth conversion remains consistent`() {
        val byteArray = byteArrayOf(1, 2, 3, 4)
        val stringified = byteArray.stringify()
        val bytified = stringified.bytify()

        assertEquals(byteArray.toList(), bytified.toList())
    }
}
