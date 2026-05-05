package id.pbbku.mobileportal.data.mapper

import id.pbbku.mobileportal.domain.model.WilayahItem
import id.pbbku.mobileportal.domain.model.WilayahLevel
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

fun JsonElement.toWilayahItems(level: WilayahLevel): List<WilayahItem> {
    return arrayPayload()
        .mapNotNull { it.jsonObjectOrNull()?.toWilayahItemOrNull(level) }
        .distinctBy { it.code }
        .sortedBy { it.code }
}

private fun JsonObject.toWilayahItemOrNull(level: WilayahLevel): WilayahItem? {
    val code = when (level) {
        WilayahLevel.PROPINSI -> firstString("kdPropinsi", "kodePropinsi", "kode", "code")
        WilayahLevel.DATI2 -> firstString("kdDati2", "kodeDati2", "kodeKabupaten", "kode", "code")
        WilayahLevel.KECAMATAN -> firstString("kdKecamatan", "kodeKecamatan", "kode", "code")
        WilayahLevel.KELURAHAN -> firstString("kdKelurahan", "kodeKelurahan", "kode", "code")
        WilayahLevel.BLOK -> firstString("kdBlok", "kodeBlok", "blok", "kode", "code")
    } ?: return null
    val name = when (level) {
        WilayahLevel.PROPINSI -> firstString("nmPropinsi", "namaPropinsi", "nama", "name")
        WilayahLevel.DATI2 -> firstString("nmDati2", "namaDati2", "namaKabupaten", "nama", "name")
        WilayahLevel.KECAMATAN -> firstString("nmKecamatan", "namaKecamatan", "nama", "name")
        WilayahLevel.KELURAHAN -> firstString("nmKelurahan", "namaKelurahan", "nama", "name")
        WilayahLevel.BLOK -> firstString("nmBlok", "namaBlok", "nama", "name")
    } ?: "Kode $code"
    return WilayahItem(code = code, name = name, level = level)
}

private fun JsonElement.arrayPayload(): List<JsonElement> {
    return when (this) {
        is JsonArray -> this.toList()
        is JsonObject -> {
            val rows = this["rows"] as? JsonArray
            val data = this["data"] as? JsonArray
            val list = this["list"] as? JsonArray
            val result = this["result"] as? JsonArray
            rows?.toList() ?: data?.toList() ?: list?.toList() ?: result?.toList() ?: emptyList()
        }
        else -> emptyList()
    }
}

private fun JsonObject.firstString(vararg keys: String): String? {
    for (key in keys) {
        val primitive = this[key]?.jsonPrimitiveOrNull() ?: continue
        val value = primitive.content.takeIf { it.isNotBlank() && it != "null" }
        if (value != null) return value
    }
    return null
}

private fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

private fun JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? = this as? JsonPrimitive
