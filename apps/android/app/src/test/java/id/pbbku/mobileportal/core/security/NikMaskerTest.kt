package id.pbbku.mobileportal.core.security

import org.junit.Assert.assertEquals
import org.junit.Test

class NikMaskerTest {
    @Test
    fun maskNik_masksMiddleDigits() {
        assertEquals("34************12", "3404123456789012".maskNik())
    }

    @Test
    fun maskNik_ignoresSeparators() {
        assertEquals("34************12", "34-041234-567890-12".maskNik())
    }
}
