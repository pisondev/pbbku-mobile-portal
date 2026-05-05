package id.pbbku.mobileportal.data.dto

import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.serialization.Serializable

@Serializable
data class NopRequest(
    val kdPropinsi: String,
    val kdDati2: String,
    val kdKecamatan: String,
    val kdKelurahan: String,
    val kdBlok: String,
    val noUrut: String,
    val kdJnsOp: String,
) {
    companion object {
        fun fromDomain(nop: Nop): NopRequest {
            return NopRequest(
                kdPropinsi = nop.kdPropinsi,
                kdDati2 = nop.kdDati2,
                kdKecamatan = nop.kdKecamatan,
                kdKelurahan = nop.kdKelurahan,
                kdBlok = nop.kdBlok,
                noUrut = nop.noUrut,
                kdJnsOp = nop.kdJnsOp,
            )
        }
    }
}
