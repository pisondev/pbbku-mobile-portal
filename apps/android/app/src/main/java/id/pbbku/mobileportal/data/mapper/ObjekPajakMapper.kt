package id.pbbku.mobileportal.data.mapper

import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.ObjekPajakDetail
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

fun JsonElement.toObjekPajakDetailOrNull(): ObjekPajakDetail? {
    val obj = jsonObjectOrNull() ?: return null
    val subjekPajak = obj["subjekPajak"]?.jsonObjectOrNull()
    val nop = obj.toNopOrNull() ?: return null
    return ObjekPajakDetail(
        nop = nop,
        alamatObjekPajak = obj.joinAddress(
            streetKey = "jalanOp",
            blockKey = "blokKavNoOp",
            rtKey = "rtOp",
            rwKey = "rwOp",
            villageKey = "kelurahanOp",
            cityKey = null,
            postalCodeKey = null,
        ),
        luasBumi = obj.numberValue("luasBumi"),
        nilaiSistemBumi = obj.numberValue("nilaiSistemBumi") ?: obj.numberValue("njopBumi"),
        jenisBumi = obj.stringValue("jnsBumi")?.let(::mapJenisBumi),
        statusWajibPajak = obj.stringValue("kdStatusWp")?.let(::mapStatusWajibPajak),
        namaWajibPajak = subjekPajak?.stringValue("nmWp") ?: obj.stringValue("nmWp"),
        alamatWajibPajak = subjekPajak?.joinAddress(
            streetKey = "jalanWp",
            blockKey = "blokKavNoWp",
            rtKey = "rtWp",
            rwKey = "rwWp",
            villageKey = "kelurahanWp",
            cityKey = "kotaWp",
            postalCodeKey = "kdPosWp",
        ) ?: obj.stringValue("jalanWp"),
        statusPekerjaanWajibPajak = subjekPajak
            ?.stringValue("statusPekerjaanWp")
            ?.let(::mapStatusPekerjaan),
    )
}

private fun JsonObject.toObjekPajakSummaryOrNull(): ObjekPajakSummary? {
    val nop = toNopOrNull() ?: return null
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

private fun JsonObject.toNopOrNull(): Nop? {
    return Nop(
        kdPropinsi = stringValue("kdPropinsi") ?: return null,
        kdDati2 = stringValue("kdDati2") ?: return null,
        kdKecamatan = stringValue("kdKecamatan") ?: return null,
        kdKelurahan = stringValue("kdKelurahan") ?: return null,
        kdBlok = stringValue("kdBlok") ?: return null,
        noUrut = stringValue("noUrut") ?: return null,
        kdJnsOp = stringValue("kdJnsOp") ?: return null,
    )
}

private fun JsonObject.joinAddress(
    streetKey: String,
    blockKey: String?,
    rtKey: String?,
    rwKey: String?,
    villageKey: String?,
    cityKey: String?,
    postalCodeKey: String?,
): String? {
    val parts = buildList {
        stringValue(streetKey)?.let(::add)
        blockKey?.let { stringValue(it) }?.let { add("Blok/Kav $it") }
        val rt = rtKey?.let { stringValue(it) }
        val rw = rwKey?.let { stringValue(it) }
        if (rt != null || rw != null) add("RT ${rt ?: "-"} / RW ${rw ?: "-"}")
        villageKey?.let { stringValue(it) }?.let(::add)
        cityKey?.let { stringValue(it) }?.let(::add)
        postalCodeKey?.let { stringValue(it) }?.let { add("Kode Pos $it") }
    }
    return parts.joinToString(", ").takeIf { it.isNotBlank() }
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

private fun mapJenisBumi(code: String): String {
    return when (code) {
        "1" -> "Tanah dan bangunan"
        "2" -> "Kavling siap bangun"
        "3" -> "Tanah kosong"
        "4" -> "Fasilitas umum"
        else -> "Kode $code"
    }
}

private fun mapStatusWajibPajak(code: String): String {
    return when (code) {
        "1" -> "Pemilik"
        "2" -> "Penyewa"
        "3" -> "Pengelola"
        "4" -> "Pemakai"
        "5" -> "Sengketa"
        else -> "Kode $code"
    }
}

private fun mapStatusPekerjaan(code: String): String {
    return when (code) {
        "1" -> "PNS/TNI/Polri"
        "2" -> "Pegawai swasta"
        "3" -> "Wiraswasta"
        "4" -> "Pensiunan"
        "5" -> "Lainnya"
        else -> "Kode $code"
    }
}
