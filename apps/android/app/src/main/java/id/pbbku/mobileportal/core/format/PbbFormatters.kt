package id.pbbku.mobileportal.core.format

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val indonesianLocale = Locale("id", "ID")

fun Long.toRupiahText(): String {
    return NumberFormat.getCurrencyInstance(indonesianLocale).format(this)
}

fun LocalDate.toIndonesianDateText(): String {
    return format(DateTimeFormatter.ofPattern("dd MMMM yyyy", indonesianLocale))
}

fun Boolean?.toPaymentStatusText(): String {
    return when (this) {
        true -> "Lunas"
        false -> "Belum Lunas"
        null -> "Tidak Diketahui"
    }
}
