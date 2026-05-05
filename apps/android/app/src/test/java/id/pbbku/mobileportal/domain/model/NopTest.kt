package id.pbbku.mobileportal.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NopTest {
    @Test
    fun parseOrNull_keepsLeadingZeroSegments() {
        val nop = Nop.parseOrNull("32.04.010.001.001.0001.0")

        assertNotNull(nop)
        requireNotNull(nop)
        assertEquals("32", nop.kdPropinsi)
        assertEquals("04", nop.kdDati2)
        assertEquals("010", nop.kdKecamatan)
        assertEquals("001", nop.kdKelurahan)
        assertEquals("001", nop.kdBlok)
        assertEquals("0001", nop.noUrut)
        assertEquals("0", nop.kdJnsOp)
        assertEquals("320401000100100010", nop.asDisplayText())
    }

    @Test
    fun parseOrNull_rejectsInvalidLength() {
        assertNull(Nop.parseOrNull("32040100010010001"))
    }
}
