package id.pbbku.mobileportal.core.format

import id.pbbku.mobileportal.domain.model.PaymentStatus
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val indonesianLocale = Locale("id", "ID")

fun Long.toRupiahText(): String {
    return NumberFormat.getCurrencyInstance(indonesianLocale).format(this)
}

fun Double.toRupiahText(): String {
    return NumberFormat.getCurrencyInstance(indonesianLocale).format(this)
}

fun LocalDate.toIndonesianDateText(): String {
    return format(DateTimeFormatter.ofPattern("dd MMMM yyyy", indonesianLocale))
}

fun Long.toIndonesianDateTimeText(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", indonesianLocale))
}

fun Boolean?.toPaymentStatus(): PaymentStatus {
    return when (this) {
        true -> PaymentStatus.PAID
        false -> PaymentStatus.UNPAID
        null -> PaymentStatus.UNKNOWN
    }
}

fun PaymentStatus.toPaymentStatusText(): String = displayText
