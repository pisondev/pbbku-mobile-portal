package id.pbbku.mobileportal.domain.model

data class ObjekPajakDetail(
    val nop: Nop,
    val alamatObjekPajak: String?,
    val luasBumi: Double?,
    val nilaiSistemBumi: Double?,
    val jenisBumi: String?,
    val statusWajibPajak: String?,
    val namaWajibPajak: String?,
    val alamatWajibPajak: String?,
    val statusPekerjaanWajibPajak: String?,
) {
    val nopDisplay: String = nop.asDisplayText()
}
