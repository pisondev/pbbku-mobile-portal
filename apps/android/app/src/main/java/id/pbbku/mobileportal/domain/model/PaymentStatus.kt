package id.pbbku.mobileportal.domain.model

enum class PaymentStatus(val displayText: String) {
    PAID("Lunas"),
    UNPAID("Belum Lunas"),
    OVERDUE("Jatuh Tempo"),
    UNKNOWN("Tidak Diketahui"),
}
