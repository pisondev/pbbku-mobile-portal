package id.pbbku.mobileportal.domain.model

data class Nop(
    val kdPropinsi: String,
    val kdDati2: String,
    val kdKecamatan: String,
    val kdKelurahan: String,
    val kdBlok: String,
    val noUrut: String,
    val kdJnsOp: String,
) {
    fun asDisplayText(): String {
        return kdPropinsi +
            kdDati2 +
            kdKecamatan +
            kdKelurahan +
            kdBlok +
            noUrut +
            kdJnsOp
    }

    fun asGroupedText(): String {
        return listOf(
            kdPropinsi,
            kdDati2,
            kdKecamatan,
            kdKelurahan,
            kdBlok,
            noUrut,
            kdJnsOp,
        ).joinToString(".")
    }

    fun asApiMap(): Map<String, String> {
        return mapOf(
            "kdPropinsi" to kdPropinsi,
            "kdDati2" to kdDati2,
            "kdKecamatan" to kdKecamatan,
            "kdKelurahan" to kdKelurahan,
            "kdBlok" to kdBlok,
            "noUrut" to noUrut,
            "kdJnsOp" to kdJnsOp,
        )
    }

    companion object {
        const val FULL_LENGTH = 18

        fun parseOrNull(value: String): Nop? {
            val digits = value.filter(Char::isDigit)
            if (digits.length != FULL_LENGTH) return null
            return Nop(
                kdPropinsi = digits.substring(0, 2),
                kdDati2 = digits.substring(2, 4),
                kdKecamatan = digits.substring(4, 7),
                kdKelurahan = digits.substring(7, 10),
                kdBlok = digits.substring(10, 13),
                noUrut = digits.substring(13, 17),
                kdJnsOp = digits.substring(17, 18),
            )
        }
    }
}
