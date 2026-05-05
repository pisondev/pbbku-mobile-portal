package id.pbbku.mobileportal.feature.report

data class ReportDraftValidationResult(
    val newBuildingAreaError: String? = null,
    val newFloorCountError: String? = null,
    val descriptionError: String? = null,
) {
    val isValid: Boolean
        get() = newBuildingAreaError == null &&
            newFloorCountError == null &&
            descriptionError == null
}

object ReportDraftFormValidator {
    fun validate(
        newBuildingAreaText: String,
        newFloorCountText: String,
        description: String,
        requireDescription: Boolean,
    ): ReportDraftValidationResult {
        val areaError = validatePositiveDouble(newBuildingAreaText, "Luas baru")
        val floorError = validatePositiveInt(newFloorCountText, "Jumlah lantai baru")
        val descriptionError = if (requireDescription && description.isBlank()) {
            "Deskripsi perubahan wajib diisi."
        } else {
            null
        }
        return ReportDraftValidationResult(
            newBuildingAreaError = areaError,
            newFloorCountError = floorError,
            descriptionError = descriptionError,
        )
    }

    fun parseOptionalDouble(value: String): Double? {
        return value.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()
    }

    fun parseOptionalInt(value: String): Int? {
        return value.trim().takeIf { it.isNotBlank() }?.toIntOrNull()
    }

    private fun validatePositiveDouble(value: String, label: String): String? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null
        val number = trimmed.toDoubleOrNull()
        return when {
            number == null -> "$label harus berupa angka valid."
            number < 0.0 -> "$label tidak boleh negatif."
            else -> null
        }
    }

    private fun validatePositiveInt(value: String, label: String): String? {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return null
        val number = trimmed.toIntOrNull()
        return when {
            number == null -> "$label harus berupa angka bulat valid."
            number < 0 -> "$label tidak boleh negatif."
            else -> null
        }
    }
}
