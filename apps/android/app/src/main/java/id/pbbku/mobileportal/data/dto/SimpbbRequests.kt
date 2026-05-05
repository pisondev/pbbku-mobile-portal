package id.pbbku.mobileportal.data.dto

import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.serialization.Serializable

@Serializable
data class ObjekPajakSearchRequest(
    val query: String,
    val limit: Int? = null,
)

@Serializable
data class ObjekPajakListDetailsRequest(
    val kdPropinsi: String? = null,
    val kdDati2: String? = null,
    val limit: Int,
    val offset: Int,
    val search: String? = null,
)

@Serializable
data class BuildingRequest(
    val kdPropinsi: String,
    val kdDati2: String,
    val kdKecamatan: String,
    val kdKelurahan: String,
    val kdBlok: String,
    val noUrut: String,
    val kdJnsOp: String,
    val noBng: Int,
) {
    companion object {
        fun fromDomain(nop: Nop, noBng: String): BuildingRequest {
            return BuildingRequest(
                kdPropinsi = nop.kdPropinsi,
                kdDati2 = nop.kdDati2,
                kdKecamatan = nop.kdKecamatan,
                kdKelurahan = nop.kdKelurahan,
                kdBlok = nop.kdBlok,
                noUrut = nop.noUrut,
                kdJnsOp = nop.kdJnsOp,
                noBng = noBng.toIntOrNull() ?: 0,
            )
        }
    }
}

@Serializable
data class SpptGetRequest(
    val kdPropinsi: String,
    val kdDati2: String,
    val kdKecamatan: String,
    val kdKelurahan: String,
    val kdBlok: String,
    val noUrut: String,
    val kdJnsOp: String,
    val thnPajakSppt: Int,
) {
    companion object {
        fun fromDomain(nop: Nop, thnPajakSppt: Int): SpptGetRequest {
            return SpptGetRequest(
                kdPropinsi = nop.kdPropinsi,
                kdDati2 = nop.kdDati2,
                kdKecamatan = nop.kdKecamatan,
                kdKelurahan = nop.kdKelurahan,
                kdBlok = nop.kdBlok,
                noUrut = nop.noUrut,
                kdJnsOp = nop.kdJnsOp,
                thnPajakSppt = thnPajakSppt,
            )
        }
    }
}

@Serializable
data class SpptListRequest(
    val thnPajak: Int? = null,
    val kdPropinsi: String? = null,
    val statusPembayaran: String? = null,
    val limit: Int,
    val offset: Int,
)

@Serializable
data class WilayahDati2Request(
    val kdPropinsi: String,
)

@Serializable
data class WilayahKecamatanRequest(
    val kdPropinsi: String,
    val kdDati2: String,
)

@Serializable
data class WilayahKelurahanRequest(
    val kdPropinsi: String,
    val kdDati2: String,
    val kdKecamatan: String,
)

@Serializable
data class WilayahBlokRequest(
    val kdPropinsi: String,
    val kdDati2: String,
    val kdKecamatan: String,
    val kdKelurahan: String,
)
