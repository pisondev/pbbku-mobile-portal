package id.pbbku.mobileportal.data.demo

import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.core.security.maskNik
import id.pbbku.mobileportal.data.session.SessionDataStore
import id.pbbku.mobileportal.domain.model.BuildingDetail
import id.pbbku.mobileportal.domain.model.BuildingFacility
import id.pbbku.mobileportal.domain.model.BuildingSummary
import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.ObjekPajakDetail
import id.pbbku.mobileportal.domain.model.ObjekPajakPage
import id.pbbku.mobileportal.domain.model.ObjekPajakSummary
import id.pbbku.mobileportal.domain.model.PaymentAttempt
import id.pbbku.mobileportal.domain.model.PaymentAttemptStatus
import id.pbbku.mobileportal.domain.model.PaymentFlowData
import id.pbbku.mobileportal.domain.model.PaymentStatus
import id.pbbku.mobileportal.domain.model.SspdReceipt
import id.pbbku.mobileportal.domain.model.TaxBillDetail
import id.pbbku.mobileportal.domain.model.TaxBillSummary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

class NikScopedDemoRepository(
    private val sessionDataStore: SessionDataStore,
) {
    private val paidOverrides = mutableMapOf<String, Long>()
    private val paymentAttempts = mutableMapOf<String, PaymentAttempt>()
    private val pendingPaymentBySppt = mutableMapOf<String, String>()
    private val successfulPaymentBySppt = mutableMapOf<String, String>()
    private val receiptsByPayment = mutableMapOf<String, SspdReceipt>()
    private var runningNumber = 1

    suspend fun currentNik(): String? {
        return sessionDataStore.session.first().nik?.takeIf { it.isNotBlank() }
    }

    suspend fun searchObjekPajak(
        query: String,
        limit: Int,
    ): AppResult<List<ObjekPajakSummary>> {
        val nik = currentNik() ?: return AppResult.Error(LOGIN_REQUIRED_MESSAGE)
        val rows = DemoTaxpayerDirectory.recordsForNik(nik)
            .map { it.summary }
            .filter { it.matchesQuery(query) }
            .take(limit)
        return AppResult.Success(rows)
    }

    suspend fun listObjekPajak(
        kdPropinsi: String?,
        kdDati2: String?,
        kdKecamatan: String?,
        kdKelurahan: String?,
        kdBlok: String?,
        search: String?,
        limit: Int,
        offset: Int,
    ): AppResult<ObjekPajakPage> {
        val nik = currentNik() ?: return AppResult.Error(LOGIN_REQUIRED_MESSAGE)
        val rows = DemoTaxpayerDirectory.recordsForNik(nik)
            .map { it.summary }
            .filter { summary ->
                kdPropinsi?.let { summary.nop.kdPropinsi == it } ?: true
            }
            .filter { summary ->
                kdDati2?.let { summary.nop.kdDati2 == it } ?: true
            }
            .filter { summary ->
                kdKecamatan?.let { summary.nop.kdKecamatan == it } ?: true
            }
            .filter { summary ->
                kdKelurahan?.let { summary.nop.kdKelurahan == it } ?: true
            }
            .filter { summary ->
                kdBlok?.let { summary.nop.kdBlok == it } ?: true
            }
            .filter { summary ->
                search?.takeIf { it.isNotBlank() }?.let { query -> summary.matchesQuery(query) } ?: true
            }
        return AppResult.Success(
            ObjekPajakPage(
                rows = rows.drop(offset).take(limit),
                total = rows.size,
            ),
        )
    }

    suspend fun getObjekPajakDetail(nop: Nop): AppResult<ObjekPajakDetail> {
        return requireRecord(nop) { it.detail }
    }

    suspend fun listBangunan(nop: Nop): AppResult<List<BuildingSummary>> {
        return requireRecord(nop) { it.buildingSummaries }
    }

    suspend fun getBuilding(nop: Nop, noBng: String): AppResult<BuildingDetail> {
        return requireRecord(nop) { record ->
            record.buildings.firstOrNull { it.noBng == noBng }
        }
    }

    suspend fun listFacilities(nop: Nop, noBng: String): AppResult<List<BuildingFacility>> {
        return requireRecord(nop) { record ->
            if (record.buildings.any { it.noBng == noBng }) record.facilities else null
        }
    }

    suspend fun listTaxBills(nop: Nop): AppResult<List<TaxBillSummary>> {
        return requireRecord(nop) { record ->
            record.taxBills
                .map { detailWithPaymentState(it).toSummary() }
                .sortedByDescending { it.taxYear }
        }
    }

    suspend fun listActiveTaxBills(nop: Nop): AppResult<List<TaxBillSummary>> {
        return requireRecord(nop) { record ->
            record.taxBills
                .map { detailWithPaymentState(it).toSummary() }
                .filter { it.isOverdue }
                .sortedByDescending { it.taxYear }
        }
    }

    suspend fun getTaxBill(nop: Nop, taxYear: Int): AppResult<TaxBillDetail> {
        return requireRecord(nop) { record ->
            record.taxBills
                .firstOrNull { it.taxYear == taxYear }
                ?.let { detailWithPaymentState(it) }
        }
    }

    suspend fun getOrCreatePaymentFlow(nop: Nop, taxYear: Int): AppResult<PaymentFlowData> {
        return requireRecord(nop) { record ->
            val detail = record.taxBills
                .firstOrNull { it.taxYear == taxYear }
                ?.let { detailWithPaymentState(it) }
                ?: return@requireRecord null
            val spptId = spptId(nop, taxYear)
            val receipt = receiptForSppt(record, spptId, detail)
            val payment = when {
                detail.isPaid -> receipt?.let { paymentAttempts[it.paymentId] }
                else -> getOrCreatePendingPayment(record, detail)
            }
            PaymentFlowData(
                taxBill = detail,
                taxpayerName = record.detail.namaWajibPajak,
                maskedNik = record.ownerNik.maskNik(),
                objectAddress = record.detail.alamatObjekPajak,
                paymentAttempt = payment,
                receipt = receipt,
            )
        }
    }

    suspend fun simulatePaymentSuccess(paymentId: String): AppResult<PaymentFlowData> {
        val nik = currentNik() ?: return AppResult.Error(LOGIN_REQUIRED_MESSAGE)
        val existing = paymentAttempts[paymentId]
            ?: return AppResult.Error("Payment attempt tidak ditemukan.")
        if (existing.nik != nik) {
            return AppResult.Error("Akses ditolak. Payment attempt ini tidak milik NIK yang sedang login.")
        }
        val record = DemoTaxpayerDirectory.recordForNikAndNop(nik, existing.nop)
            ?: return AppResult.Error(accessDeniedMessage(existing.nop))
        val currentDetail = record.taxBills
            .firstOrNull { it.taxYear == existing.taxYear }
            ?.let { detailWithPaymentState(it) }
            ?: return AppResult.Error("SPPT untuk payment ini tidak ditemukan.")
        val spptKey = spptId(existing.nop, existing.taxYear)
        val now = System.currentTimeMillis()

        val successPayment = when (existing.status) {
            PaymentAttemptStatus.SUCCESS -> existing
            else -> {
                if (currentDetail.isPaid) {
                    return AppResult.Error("SPPT tahun ${existing.taxYear} sudah lunas dan tidak dapat dibayar ulang.")
                }
                val updated = existing.copy(
                    status = PaymentAttemptStatus.SUCCESS,
                    paidAtEpochMillis = now,
                )
                paymentAttempts[paymentId] = updated
                pendingPaymentBySppt.remove(spptKey)
                successfulPaymentBySppt[spptKey] = paymentId
                paidOverrides[spptKey] = now
                updated
            }
        }
        val paidDetail = record.taxBills
            .first { it.taxYear == successPayment.taxYear }
            .let { detailWithPaymentState(it) }
        val receipt = receiptsByPayment.getOrPut(paymentId) {
            buildReceipt(record, successPayment, paidDetail, now)
        }
        return AppResult.Success(
            PaymentFlowData(
                taxBill = paidDetail,
                taxpayerName = record.detail.namaWajibPajak,
                maskedNik = record.ownerNik.maskNik(),
                objectAddress = record.detail.alamatObjekPajak,
                paymentAttempt = successPayment,
                receipt = receipt,
            ),
        )
    }

    suspend fun canAccess(nop: Nop): Boolean {
        val nik = currentNik() ?: return false
        return DemoTaxpayerDirectory.recordForNikAndNop(nik, nop) != null
    }

    private suspend fun <T> requireRecord(
        nop: Nop,
        transform: (DemoTaxObjectRecord) -> T?,
    ): AppResult<T> {
        val nik = currentNik() ?: return AppResult.Error(LOGIN_REQUIRED_MESSAGE)
        val record = DemoTaxpayerDirectory.recordForNikAndNop(nik, nop)
            ?: return AppResult.Error(accessDeniedMessage(nop))
        return transform(record)?.let { AppResult.Success(it) } ?: AppResult.Empty
    }

    private fun ObjekPajakSummary.matchesQuery(query: String): Boolean {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return true
        return listOf(
            nop.asDisplayText(),
            nop.asGroupedText(),
            namaWajibPajak.orEmpty(),
            alamatObjekPajak.orEmpty(),
        ).any { it.lowercase().contains(normalized) }
    }

    private fun accessDeniedMessage(nop: Nop): String {
        val owner = DemoTaxpayerDirectory.ownerNikForNop(nop)
        return if (owner == null) {
            "NOP ini tidak tersedia di data demo wajib pajak."
        } else {
            "Akses ditolak. NOP ini tidak terhubung dengan NIK yang sedang login."
        }
    }

    private fun getOrCreatePendingPayment(
        record: DemoTaxObjectRecord,
        detail: TaxBillDetail,
    ): PaymentAttempt {
        val spptId = spptId(detail.nop, detail.taxYear)
        pendingPaymentBySppt[spptId]?.let { paymentId ->
            paymentAttempts[paymentId]?.takeIf { it.status == PaymentAttemptStatus.PENDING }?.let { return it }
        }
        val now = System.currentTimeMillis()
        val principal = detail.amount ?: detail.pbbTerutang ?: 0.0
        val penalty = if (detail.isOverdue) detail.fine ?: 0.0 else 0.0
        val payment = PaymentAttempt(
            id = "PAY-$spptId-${now.toString().takeLast(6)}",
            spptId = spptId,
            nop = detail.nop,
            nik = record.ownerNik,
            taxYear = detail.taxYear,
            principalAmount = principal,
            penaltyAmount = penalty,
            totalAmount = principal + penalty,
            method = if (detail.isOverdue) "Virtual Account" else "QRIS Demo",
            paymentCode = if (detail.isOverdue) {
                "VA-${detail.nop.noUrut}${detail.taxYear}"
            } else {
                "QRIS-${detail.nop.noUrut}-${detail.taxYear}"
            },
            status = PaymentAttemptStatus.PENDING,
            createdAtEpochMillis = now,
            paidAtEpochMillis = null,
        )
        paymentAttempts[payment.id] = payment
        pendingPaymentBySppt[spptId] = payment.id
        return payment
    }

    private fun receiptForSppt(
        record: DemoTaxObjectRecord,
        spptId: String,
        detail: TaxBillDetail,
    ): SspdReceipt? {
        successfulPaymentBySppt[spptId]?.let { paymentId ->
            receiptsByPayment[paymentId]?.let { return it }
        }
        if (!detail.isPaid || detail.paymentDate == null) return null
        val historicalPayment = paymentAttempts.values.firstOrNull {
            it.spptId == spptId && it.status == PaymentAttemptStatus.SUCCESS
        } ?: buildHistoricalPayment(record, detail)
        successfulPaymentBySppt[spptId] = historicalPayment.id
        return receiptsByPayment.getOrPut(historicalPayment.id) {
            buildReceipt(
                record = record,
                payment = historicalPayment,
                detail = detail,
                issuedAt = historicalPayment.paidAtEpochMillis ?: System.currentTimeMillis(),
            )
        }
    }

    private fun buildHistoricalPayment(
        record: DemoTaxObjectRecord,
        detail: TaxBillDetail,
    ): PaymentAttempt {
        val spptId = spptId(detail.nop, detail.taxYear)
        val paidAt = detail.paymentDate
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
            ?: System.currentTimeMillis()
        val principal = detail.amount ?: detail.pbbTerutang ?: 0.0
        val penalty = detail.fine ?: 0.0
        val payment = PaymentAttempt(
            id = "PAY-HIST-$spptId",
            spptId = spptId,
            nop = detail.nop,
            nik = record.ownerNik,
            taxYear = detail.taxYear,
            principalAmount = principal,
            penaltyAmount = penalty,
            totalAmount = principal + penalty,
            method = "Bank Transfer",
            paymentCode = "HIST-${detail.nop.noUrut}-${detail.taxYear}",
            status = PaymentAttemptStatus.SUCCESS,
            createdAtEpochMillis = paidAt,
            paidAtEpochMillis = paidAt,
        )
        paymentAttempts[payment.id] = payment
        return payment
    }

    private fun buildReceipt(
        record: DemoTaxObjectRecord,
        payment: PaymentAttempt,
        detail: TaxBillDetail,
        issuedAt: Long,
    ): SspdReceipt {
        val number = "SSPD-${payment.taxYear}-${payment.nop.noUrut}-${runningNumber.toString().padStart(4, '0')}"
        runningNumber += 1
        return SspdReceipt(
            id = "SSPD-${payment.id}",
            paymentId = payment.id,
            spptId = payment.spptId,
            nop = payment.nop,
            nik = payment.nik,
            sspdNumber = number,
            taxYear = payment.taxYear,
            principalAmount = payment.principalAmount,
            penaltyAmount = payment.penaltyAmount,
            totalPaid = payment.totalAmount,
            method = payment.method,
            issuedAtEpochMillis = issuedAt,
            namaWajibPajak = record.detail.namaWajibPajak,
            alamatObjekPajak = record.detail.alamatObjekPajak,
        )
    }

    private fun detailWithPaymentState(detail: TaxBillDetail): TaxBillDetail {
        val paidAt = paidOverrides[spptId(detail.nop, detail.taxYear)]
        return if (paidAt != null) {
            detail.copy(
                status = PaymentStatus.PAID,
                paymentDate = Instant.ofEpochMilli(paidAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(),
            )
        } else {
            val status = if (detail.status != PaymentStatus.PAID && detail.dueDate?.isBefore(LocalDate.now()) == true) {
                PaymentStatus.OVERDUE
            } else {
                detail.status
            }
            detail.copy(status = status)
        }
    }

    private fun TaxBillDetail.toSummary(): TaxBillSummary {
        return TaxBillSummary(
            nop = nop,
            taxYear = taxYear,
            amount = amount,
            status = status,
            dueDate = dueDate,
            fine = fine,
        )
    }

    private fun spptId(nop: Nop, taxYear: Int): String {
        return "${nop.asDisplayText()}-$taxYear"
    }

    companion object {
        const val LOGIN_REQUIRED_MESSAGE = "Sesi login tidak aktif. Masuk kembali untuk melihat data wajib pajak."
    }
}
