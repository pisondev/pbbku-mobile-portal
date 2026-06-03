package id.pbbku.mobileportal.data.api

import id.pbbku.mobileportal.BuildConfig

object SimpbbApiConfig {
    const val DEFAULT_LOCAL_BASE_URL = "http://10.0.2.2:8080/api/rpc/"
    const val DEFAULT_PRODUCTION_BASE_URL = "https://pbbku-api.tierratie.com/api/rpc/"
    val BASE_URL: String = BuildConfig.PBBKU_API_BASE_URL

    object Endpoint {
        const val OBJEK_PAJAK_SEARCH = "objekPajak/search"
        const val OBJEK_PAJAK_LIST_DETAILS = "objekPajak/listDetails"
        const val OBJEK_PAJAK_GET_BY_NOP = "objekPajak/getByNop"
        const val OBJEK_PAJAK_GET_SPPT_HISTORY = "objekPajak/getSpptHistory"
        const val OBJEK_PAJAK_GET_TUNGGAKAN = "objekPajak/getTunggakan"

        const val LSPOP_LIST_BY_NOP = "lspop/listByNop"
        const val LSPOP_GET_BUILDING = "lspop/getBuilding"
        const val LSPOP_LIST_FASILITAS = "lspop/listFasilitas"

        const val SPPT_LIST_BY_NOP = "sppt/listByNop"
        const val SPPT_GET = "sppt/get"
        const val SPPT_LIST = "sppt/list"

        const val WILAYAH_LIST_PROPINSI = "wilayah/listPropinsi"
        const val WILAYAH_LIST_DATI2 = "wilayah/listDati2"
        const val WILAYAH_LIST_KECAMATAN = "wilayah/listKecamatan"
        const val WILAYAH_LIST_KELURAHAN = "wilayah/listKelurahan"
        const val WILAYAH_LIST_BLOK = "wilayah/listBlok"
    }
}
