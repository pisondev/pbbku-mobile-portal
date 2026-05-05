package id.pbbku.mobileportal.domain.model

data class BuildingSummary(
    val nop: Nop,
    val noBng: String,
    val luasBangunan: Double?,
    val jumlahLantai: Int?,
    val jenisBangunan: String?,
    val jpb: String?,
    val nilaiSistemBangunan: Double?,
) {
    val label: String = "Bangunan $noBng"
}

data class BuildingDetail(
    val nop: Nop,
    val noBng: String,
    val luasBangunan: Double?,
    val jumlahLantai: Int?,
    val jenisBangunan: String?,
    val jpb: String?,
    val tahunDibangun: Int?,
    val tahunRenovasi: Int?,
    val kondisi: String?,
    val konstruksi: String?,
    val atap: String?,
    val dinding: String?,
    val lantai: String?,
    val langitLangit: String?,
    val nilaiSistemBangunan: Double?,
)

data class BuildingFacility(
    val name: String,
    val quantity: Double?,
    val unit: String?,
    val description: String?,
)
