package id.pbbku.mobileportal.domain.model

enum class PaymentAttemptStatus(val label: String) {
    PENDING("Menunggu Pembayaran"),
    SUCCESS("Berhasil"),
    FAILED("Gagal"),
    CANCELLED("Dibatalkan"),
}

data class PaymentAttempt(
    val id: String,
    val spptId: String,
    val nop: Nop,
    val nik: String,
    val taxYear: Int,
    val principalAmount: Double,
    val penaltyAmount: Double,
    val totalAmount: Double,
    val method: String,
    val paymentCode: String,
    val status: PaymentAttemptStatus,
    val createdAtEpochMillis: Long,
    val paidAtEpochMillis: Long?,
)

data class SspdReceipt(
    val id: String,
    val paymentId: String,
    val spptId: String,
    val nop: Nop,
    val nik: String,
    val sspdNumber: String,
    val taxYear: Int,
    val principalAmount: Double,
    val penaltyAmount: Double,
    val totalPaid: Double,
    val method: String,
    val issuedAtEpochMillis: Long,
    val namaWajibPajak: String?,
    val alamatObjekPajak: String?,
)

data class PaymentFlowData(
    val taxBill: TaxBillDetail,
    val taxpayerName: String?,
    val maskedNik: String,
    val objectAddress: String?,
    val paymentAttempt: PaymentAttempt?,
    val receipt: SspdReceipt?,
)
