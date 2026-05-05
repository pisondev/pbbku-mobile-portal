package id.pbbku.mobileportal.data.mapper

import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.PaymentStatus
import id.pbbku.mobileportal.domain.model.TaxBillDetail
import id.pbbku.mobileportal.domain.model.TaxBillSummary
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

fun JsonElement.toTaxBillSummaries(nop: Nop): List<TaxBillSummary> {
    return arrayPayload()
        .mapNotNull { it.jsonObjectOrNull()?.toTaxBillSummaryOrNull(nop) }
        .sortedByDescending { it.taxYear }
}

fun JsonElement.toTaxBillDetailOrNull(nop: Nop, fallbackTaxYear: Int): TaxBillDetail? {
    val obj = when (this) {
        is JsonObject -> this["data"]?.jsonObjectOrNull() ?: this
        else -> null
    } ?: return null
    val taxYear = obj.readTaxYear() ?: fallbackTaxYear
    return TaxBillDetail(
        nop = nop,
        taxYear = taxYear,
        status = obj.readPaymentStatus(),
        amount = obj.readAmount(),
        dueDate = obj.readDate("tglJatuhTempoSppt", "tglJatuhTempo", "jatuhTempo"),
        fine = obj.firstNumber("denda", "dendaPbb", "dendaSppt", "totalDenda"),
        paymentDate = obj.readDate("tglPembayaranSppt", "tglPembayaran", "tanggalBayar"),
        njopBumi = obj.firstNumber("njopBumi", "njopBumiSppt", "nilaiSistemBumi"),
        njopBangunan = obj.firstNumber("njopBng", "njopBangunan", "njopBangunanSppt", "nilaiSistemBng"),
        njopTotal = obj.firstNumber("njopTotal", "totalNjop", "njopSppt"),
        njoptkp = obj.firstNumber("njoptkp", "njoptkpSppt", "njopTidakKenaPajak"),
        tarif = obj.firstNumber("tarif", "tarifPbb", "tarifSppt"),
        pbbTerutang = obj.firstNumber("pbbTerutang", "pbbTerhutang", "pbbYgHarusDibayarSppt"),
    )
}

private fun JsonObject.toTaxBillSummaryOrNull(nop: Nop): TaxBillSummary? {
    val taxYear = readTaxYear() ?: return null
    val dueDate = readDate("tglJatuhTempoSppt", "tglJatuhTempo", "jatuhTempo")
    val status = readPaymentStatus(dueDate)
    return TaxBillSummary(
        nop = nop,
        taxYear = taxYear,
        amount = readAmount(),
        status = status,
        dueDate = dueDate,
        fine = firstNumber("denda", "dendaPbb", "dendaSppt", "totalDenda"),
    )
}

private fun JsonObject.readTaxYear(): Int? {
    return firstInt("thnPajakSppt", "thnPajak", "tahunPajak", "tahun")
}

private fun JsonObject.readAmount(): Double? {
    return firstNumber(
        "pbbYgHarusDibayarSppt",
        "spptYgHarusDibayar",
        "pbbTerutang",
        "pbbTerhutang",
        "jumlahPbb",
        "jumlahTagihan",
        "totalTagihan",
        "nominal",
        "pokok",
    )
}

private fun JsonObject.readPaymentStatus(dueDate: LocalDate? = null): PaymentStatus {
    val explicit = firstString(
        "statusPembayaran",
        "statusBayar",
        "statusPembayaranSppt",
        "stsPembayaranSppt",
        "status",
    )?.lowercase()
    val paid = firstString("lunas", "isPaid", "paid")?.lowercase()
    val paymentDate = readDate("tglPembayaranSppt", "tglPembayaran", "tanggalBayar")
    val baseStatus = when {
        paymentDate != null -> PaymentStatus.PAID
        paid in setOf("true", "1", "ya", "y") -> PaymentStatus.PAID
        paid in setOf("false", "0", "tidak", "n") -> PaymentStatus.UNPAID
        explicit in setOf("1", "lunas", "paid", "sudah bayar", "terbayar") -> PaymentStatus.PAID
        explicit in setOf("0", "belum", "belum lunas", "unpaid", "belum bayar") -> PaymentStatus.UNPAID
        else -> PaymentStatus.UNKNOWN
    }
    return if (
        baseStatus == PaymentStatus.UNPAID &&
        dueDate != null &&
        dueDate.isBefore(LocalDate.now())
    ) {
        PaymentStatus.OVERDUE
    } else {
        baseStatus
    }
}

private fun JsonElement.arrayPayload(): List<JsonElement> {
    return when (this) {
        is JsonArray -> this.toList()
        is JsonObject -> {
            val rows = this["rows"] as? JsonArray
            val data = this["data"] as? JsonArray
            val list = this["list"] as? JsonArray
            rows?.toList() ?: data?.toList() ?: list?.toList() ?: emptyList()
        }
        else -> emptyList()
    }
}

private fun JsonObject.readDate(vararg keys: String): LocalDate? {
    for (key in keys) {
        val raw = firstString(key) ?: continue
        val parsed = parseDate(raw)
        if (parsed != null) return parsed
    }
    return null
}

private fun parseDate(raw: String): LocalDate? {
    val text = raw.trim()
    return runCatching { OffsetDateTime.parse(text).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDate.parse(text) }.getOrNull()
        ?: runCatching { LocalDate.parse(text, DateTimeFormatter.ofPattern("dd-MM-yyyy")) }.getOrNull()
        ?: runCatching { LocalDate.parse(text, DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrNull()
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
