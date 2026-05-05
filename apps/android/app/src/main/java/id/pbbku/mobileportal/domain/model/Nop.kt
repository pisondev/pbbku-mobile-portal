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
}
