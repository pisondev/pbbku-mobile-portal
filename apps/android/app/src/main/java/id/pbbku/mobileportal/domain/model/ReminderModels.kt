package id.pbbku.mobileportal.domain.model

import java.time.LocalDate

enum class ReminderStatus(val displayText: String) {
    SCHEDULED("Terjadwal"),
    ACTIVE("Aktif"),
    UNAVAILABLE("Tidak Tersedia"),
    SIMULATION("Prioritas Tagihan"),
}

data class PaymentReminder(
    val id: String,
    val nop: Nop,
    val taxYear: Int,
    val amount: Double?,
    val dueDate: LocalDate?,
    val offsetDays: Int?,
    val scheduledAtEpochMillis: Long?,
    val status: ReminderStatus,
    val note: String,
) {
    val isSimulation: Boolean = status == ReminderStatus.SIMULATION
}
