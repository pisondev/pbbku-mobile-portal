package id.pbbku.mobileportal.domain.model

import java.time.LocalDate

data class TaxBillSummary(
    val nop: Nop,
    val taxYear: Int,
    val amount: Double?,
    val status: PaymentStatus,
    val dueDate: LocalDate?,
    val fine: Double?,
) {
    val isPayable: Boolean = status == PaymentStatus.UNPAID || status == PaymentStatus.OVERDUE
}

data class TaxBillDetail(
    val nop: Nop,
    val taxYear: Int,
    val status: PaymentStatus,
    val amount: Double?,
    val dueDate: LocalDate?,
    val fine: Double?,
    val paymentDate: LocalDate?,
    val njopBumi: Double?,
    val njopBangunan: Double?,
    val njopTotal: Double?,
    val njoptkp: Double?,
    val tarif: Double?,
    val pbbTerutang: Double?,
) {
    val isPayable: Boolean = status == PaymentStatus.UNPAID || status == PaymentStatus.OVERDUE
}
