package id.pbbku.mobileportal.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NikValidatorTest {
    @Test
    fun isValidNik_acceptsSixteenDigits() {
        assertTrue("3404123456789012".isValidNik())
    }

    @Test
    fun isValidNik_rejectsNonDigits() {
        assertFalse("34041234567890AB".isValidNik())
    }

    @Test
    fun isValidNik_rejectsInvalidLength() {
        assertFalse("340412345678901".isValidNik())
    }
}
