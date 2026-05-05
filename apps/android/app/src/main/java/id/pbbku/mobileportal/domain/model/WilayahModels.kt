package id.pbbku.mobileportal.domain.model

enum class WilayahLevel {
    PROPINSI,
    DATI2,
    KECAMATAN,
    KELURAHAN,
    BLOK,
}

data class WilayahItem(
    val code: String,
    val name: String,
    val level: WilayahLevel,
) {
    val displayText: String = "$code - $name"
}
