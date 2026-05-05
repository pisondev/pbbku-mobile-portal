package id.pbbku.mobileportal.domain.model

data class ObjekPajakSummary(
    val nop: Nop,
    val namaWajibPajak: String?,
    val alamatObjekPajak: String?,
    val luasBumi: Double?,
    val njopBumi: Double?,
    val totalLuasBangunan: Double?,
    val totalNilaiBangunan: Double?,
) {
    val nopDisplay: String = nop.asDisplayText()
}

data class ObjekPajakPage(
    val rows: List<ObjekPajakSummary>,
    val total: Int?,
)
