package id.pbbku.mobileportal.feature.report

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportDraftFormValidatorTest {
    @Test
    fun validate_allowsDraftWithoutDescription() {
        val result = ReportDraftFormValidator.validate(
            newBuildingAreaText = "120.5",
            newFloorCountText = "2",
            description = "",
            requireDescription = false,
        )

        assertTrue(result.isValid)
        assertNull(result.descriptionError)
    }

    @Test
    fun validate_requiresDescriptionForSimulationSubmit() {
        val result = ReportDraftFormValidator.validate(
            newBuildingAreaText = "120",
            newFloorCountText = "2",
            description = "",
            requireDescription = true,
        )

        assertFalse(result.isValid)
        assertEquals("Deskripsi perubahan wajib diisi.", result.descriptionError)
    }

    @Test
    fun validate_rejectsInvalidAreaAndFloor() {
        val result = ReportDraftFormValidator.validate(
            newBuildingAreaText = "dua ratus",
            newFloorCountText = "1.5",
            description = "Luas bangunan diperbarui.",
            requireDescription = true,
        )

        assertFalse(result.isValid)
        assertEquals("Luas baru harus berupa angka valid.", result.newBuildingAreaError)
        assertEquals("Jumlah lantai baru harus berupa angka bulat valid.", result.newFloorCountError)
    }
}
