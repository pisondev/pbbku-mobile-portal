package id.pbbku.mobileportal.data.mapper

import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.ObjekPajakPage
import id.pbbku.mobileportal.domain.model.ObjekPajakSummary
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonElement.toObjekPajakSearchRows(): List<ObjekPajakSummary> {
    val array = this as? JsonArray ?: return emptyList()
    return array.mapNotNull { it.jsonObjectOrNull()?.toObjekPajakSummaryOrNull() }
}

fun JsonElement.toObjekPajakPage(): ObjekPajakPage {
    val obj = jsonObjectOrNull() ?: return ObjekPajakPage(rows = emptyList(), total = null)
    val rows = obj["rows"]
        ?.jsonArrayOrNull()
        ?.mapNotNull { it.jsonObjectOrNull()?.toObjekPajakSummaryOrNull() }
        .orEmpty()
    val total = obj["total"]?.jsonPrimitiveOrNull()?.intOrNull
    return ObjekPajakPage(rows = rows, total = total)
}

private fun JsonObject.toObjekPajakSummaryOrNull(): ObjekPajakSummary? {
    val nop = Nop(
        kdPropinsi = stringValue("kdPropinsi") ?: return null,
        kdDati2 = stringValue("kdDati2") ?: return null,
        kdKecamatan = stringValue("kdKecamatan") ?: return null,
        kdKelurahan = stringValue("kdKelurahan") ?: return null,
        kdBlok = stringValue("kdBlok") ?: return null,
        noUrut = stringValue("noUrut") ?: return null,
        kdJnsOp = stringValue("kdJnsOp") ?: return null,
    )
    return ObjekPajakSummary(
        nop = nop,
        namaWajibPajak = stringValue("nmWp"),
        alamatObjekPajak = stringValue("jalanOp"),
        luasBumi = numberValue("luasBumi"),
        njopBumi = numberValue("njopBumi"),
        totalLuasBangunan = numberValue("totalLuasBng"),
        totalNilaiBangunan = numberValue("totalNilaiBng"),
    )
}

private fun JsonObject.stringValue(key: String): String? {
    return this[key]?.jsonPrimitiveOrNull()?.content?.takeIf { it.isNotBlank() }
}

private fun JsonObject.numberValue(key: String): Double? {
    val primitive = this[key]?.jsonPrimitiveOrNull() ?: return null
    return primitive.doubleOrNull ?: primitive.content.toDoubleOrNull()
}

private fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

private fun JsonElement.jsonArrayOrNull(): JsonArray? = this as? JsonArray

private fun JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? = this as? JsonPrimitive
