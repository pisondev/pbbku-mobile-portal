package id.pbbku.mobileportal.data.mapper

import id.pbbku.mobileportal.domain.model.BuildingDetail
import id.pbbku.mobileportal.domain.model.BuildingFacility
import id.pbbku.mobileportal.domain.model.BuildingSummary
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

fun JsonElement.toBuildingSummaries(nop: Nop): List<BuildingSummary> {
    return arrayPayload()
        .mapNotNull { it.jsonObjectOrNull()?.toBuildingSummaryOrNull(nop) }
}

fun JsonElement.toBuildingDetailOrNull(nop: Nop, fallbackNoBng: String): BuildingDetail? {
    val obj = when (this) {
        is JsonObject -> this["data"]?.jsonObjectOrNull() ?: this
        else -> null
    } ?: return null
    return obj.toBuildingDetailOrNull(nop, fallbackNoBng)
}

fun JsonElement.toBuildingFacilities(): List<BuildingFacility> {
    return arrayPayload()
        .mapNotNull { it.jsonObjectOrNull()?.toBuildingFacilityOrNull() }
}

private fun JsonObject.toBuildingSummaryOrNull(nop: Nop): BuildingSummary? {
    val noBng = firstString("noBng", "noBangunan", "nomorBangunan") ?: return null
    return BuildingSummary(
        nop = nop,
        noBng = noBng,
        luasBangunan = firstNumber("luasBng", "luasBangunan", "luasBngTotal"),
        jumlahLantai = firstInt("jmlLantaiBng", "jumlahLantai", "jmlLantai"),
        jenisBangunan = firstString("jenisBangunan", "jnsBangunan", "jnsBng"),
        jpb = readJpb(),
        nilaiSistemBangunan = firstNumber("nilaiSistemBng", "nilaiSistemBangunan", "njopBng"),
    )
}

private fun JsonObject.toBuildingDetailOrNull(nop: Nop, fallbackNoBng: String): BuildingDetail? {
    val noBng = firstString("noBng", "noBangunan", "nomorBangunan") ?: fallbackNoBng
    if (noBng.isBlank()) return null
    return BuildingDetail(
        nop = nop,
        noBng = noBng,
        luasBangunan = firstNumber("luasBng", "luasBangunan", "luasBngTotal"),
        jumlahLantai = firstInt("jmlLantaiBng", "jumlahLantai", "jmlLantai"),
        jenisBangunan = firstString("jenisBangunan", "jnsBangunan", "jnsBng"),
        jpb = readJpb(),
        tahunDibangun = firstInt("thnDibangunBng", "tahunDibangun", "thnDibangun"),
        tahunRenovasi = firstInt("thnRenovasiBng", "tahunRenovasi", "thnRenovasi"),
        kondisi = firstString("kondisiBng", "kondisiBangunan", "kondisi"),
        konstruksi = firstString("konstruksiBng", "konstruksiBangunan", "konstruksi"),
        atap = firstString("atapBng", "atap"),
        dinding = firstString("dindingBng", "dinding"),
        lantai = firstString("lantaiBng", "lantai"),
        langitLangit = firstString("langitLangitBng", "langitLangit"),
        nilaiSistemBangunan = firstNumber("nilaiSistemBng", "nilaiSistemBangunan", "njopBng"),
    )
}

private fun JsonObject.toBuildingFacilityOrNull(): BuildingFacility? {
    val name = firstString(
        "namaFasilitas",
        "nmFasilitas",
        "fasilitas",
        "jenisFasilitas",
        "kdFasilitas",
    ) ?: return null
    return BuildingFacility(
        name = name,
        quantity = firstNumber("jumlah", "nilai", "volume", "qty"),
        unit = firstString("satuan", "unit"),
        description = firstString("keterangan", "deskripsi", "catatan"),
    )
}

private fun JsonElement.arrayPayload(): List<JsonElement> {
    return when (this) {
        is JsonArray -> this.toList()
        is JsonObject -> {
            val rows = this["rows"] as? JsonArray
            val data = this["data"] as? JsonArray
            rows?.toList() ?: data?.toList() ?: emptyList()
        }
        else -> emptyList()
    }
}

private fun JsonObject.readJpb(): String? {
    val direct = firstString("jpb", "namaJpb", "nmJpb", "jenisPenggunaanBangunan")
    if (direct != null) return direct
    val nested = this["jpb"]?.jsonObjectOrNull()
        ?: this["refJpb"]?.jsonObjectOrNull()
        ?: this["jenisPenggunaanBangunan"]?.jsonObjectOrNull()
    return nested?.firstString("nama", "nmJpb", "namaJpb", "label", "kdJpb")
        ?: firstString("kdJpb")
}

private fun JsonObject.firstString(vararg keys: String): String? {
    for (key in keys) {
        val primitive = this[key]?.jsonPrimitiveOrNull() ?: continue
        val value = primitive.content.takeIf { it.isNotBlank() && it != "null" }
        if (value != null) return value
    }
    return null
}

private fun JsonObject.firstNumber(vararg keys: String): Double? {
    for (key in keys) {
        val primitive = this[key]?.jsonPrimitiveOrNull() ?: continue
        val value = primitive.doubleOrNull ?: primitive.content.toDoubleOrNull()
        if (value != null) return value
    }
    return null
}

private fun JsonObject.firstInt(vararg keys: String): Int? {
    for (key in keys) {
        val primitive = this[key]?.jsonPrimitiveOrNull() ?: continue
        val value = primitive.intOrNull ?: primitive.content.toIntOrNull()
        if (value != null) return value
    }
    return null
}

private fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

private fun JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? = this as? JsonPrimitive
