package id.pbbku.mobileportal.data.reminder

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import id.pbbku.mobileportal.data.demo.DemoTaxpayerDirectory
import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.PaymentReminder
import id.pbbku.mobileportal.domain.model.ReminderStatus
import id.pbbku.mobileportal.domain.model.TaxBillSummary
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.reminderDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pbbku_reminders",
)

class PaymentReminderRepository(
    private val context: Context,
) {
    private val dataStore = context.reminderDataStore
    private val workManager = WorkManager.getInstance(context)

    val reminderEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.REMINDER_ENABLED] ?: false
    }

    val reminders: Flow<List<PaymentReminder>> = dataStore.data.map { preferences ->
        preferences[Keys.REMINDERS_JSON].decodeReminderRecords()
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.REMINDER_ENABLED] = enabled
        }
        if (!enabled) {
            workManager.cancelAllWorkByTag(WORK_TAG)
            saveReminders(emptyList())
        }
    }

    suspend fun scheduleForBills(bills: List<TaxBillSummary>) {
        if (!reminderEnabled.first()) return
        val reminders = bills.flatMap(::buildReminders)
        val scheduled = reminders.filter { it.status == ReminderStatus.SCHEDULED }
        scheduled.forEach(::enqueueReminder)
        saveReminders(reminders)
    }

    suspend fun ensureDemoReminderIfEmpty() {
        if (reminders.first().isNotEmpty()) return
        val now = LocalDate.now()
        val demoReminders = DemoTaxpayerDirectory.recordsForNik(DemoTaxpayerDirectory.NIK_SITI)
            .flatMap { record -> record.taxBills }
            .filter { it.isPayable }
            .sortedWith(compareBy({ it.dueDate ?: LocalDate.MAX }, { it.taxYear }))
            .take(5)
            .mapIndexed { index, bill ->
                val dueDate = bill.dueDate
                val scheduledAt = dueDate
                    ?.minusDays(30)
                    ?.takeIf { it.isAfter(now) }
                    ?.atStartOfDay(ZoneId.systemDefault())
                    ?.toInstant()
                    ?.toEpochMilli()
                PaymentReminder(
                    id = "demo-reminder-${index + 1}",
                    nop = bill.nop,
                    taxYear = bill.taxYear,
                    amount = bill.amount,
                    dueDate = dueDate,
                    offsetDays = if (scheduledAt != null) 30 else null,
                    scheduledAtEpochMillis = scheduledAt,
                    status = if (scheduledAt != null) ReminderStatus.SCHEDULED else ReminderStatus.SIMULATION,
                    note = if (scheduledAt != null) {
                        "Contoh reminder 30 hari sebelum jatuh tempo SPPT."
                    } else {
                        "Contoh notifikasi prioritas untuk tagihan yang sudah melewati jatuh tempo."
                    },
                )
            }
        saveReminders(demoReminders.ifEmpty { listOf(fallbackDemoReminder()) })
    }

    private fun fallbackDemoReminder(): PaymentReminder {
        val demoNop = Nop(
            kdPropinsi = "32",
            kdDati2 = "04",
            kdKecamatan = "010",
            kdKelurahan = "001",
            kdBlok = "001",
            noUrut = "0001",
            kdJnsOp = "0",
        )
        return PaymentReminder(
            id = "demo-reminder",
            nop = demoNop,
            taxYear = LocalDate.now().year,
            amount = null,
            dueDate = LocalDate.now().plusDays(30),
            offsetDays = 30,
            scheduledAtEpochMillis = null,
            status = ReminderStatus.SIMULATION,
            note = "Pengingat prioritas ditampilkan sambil menunggu data jatuh tempo SPPT lengkap.",
        )
    }

    private fun buildReminders(bill: TaxBillSummary): List<PaymentReminder> {
        if (!bill.isPayable) {
            workManager.cancelAllWorkByTag(workTagForBill(bill.nop, bill.taxYear))
            return emptyList()
        }
        val dueDate = bill.dueDate ?: return listOf(
            PaymentReminder(
                id = "${bill.nop.asDisplayText()}-${bill.taxYear}-no-date",
                nop = bill.nop,
                taxYear = bill.taxYear,
                amount = bill.amount,
                dueDate = null,
                offsetDays = null,
                scheduledAtEpochMillis = null,
                status = ReminderStatus.UNAVAILABLE,
                note = "Tanggal jatuh tempo belum tersedia dari data SPPT.",
            ),
        )
        val now = System.currentTimeMillis()
        return REMINDER_OFFSETS.mapNotNull { offsetDays ->
            val scheduledDate = dueDate.minusDays(offsetDays.toLong())
            val scheduledAt = scheduledDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            if (scheduledAt <= now) return@mapNotNull null
            PaymentReminder(
                id = "${bill.nop.asDisplayText()}-${bill.taxYear}-$offsetDays",
                nop = bill.nop,
                taxYear = bill.taxYear,
                amount = bill.amount,
                dueDate = dueDate,
                offsetDays = offsetDays,
                scheduledAtEpochMillis = scheduledAt,
                status = ReminderStatus.SCHEDULED,
                note = "Pengingat $offsetDays hari sebelum jatuh tempo.",
            )
        }.ifEmpty {
            listOf(
                PaymentReminder(
                    id = "${bill.nop.asDisplayText()}-${bill.taxYear}-past-date",
                    nop = bill.nop,
                    taxYear = bill.taxYear,
                    amount = bill.amount,
                    dueDate = dueDate,
                    offsetDays = null,
                    scheduledAtEpochMillis = null,
                    status = ReminderStatus.UNAVAILABLE,
                    note = "Jadwal pengingat 30/7/1 hari sudah lewat.",
                ),
            )
        }
    }

    private fun enqueueReminder(reminder: PaymentReminder) {
        val scheduledAt = reminder.scheduledAtEpochMillis ?: return
        val delayMillis = (scheduledAt - System.currentTimeMillis()).coerceAtLeast(0)
        val work = OneTimeWorkRequestBuilder<PaymentReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    PaymentReminderWorker.KEY_TAX_YEAR to reminder.taxYear,
                    PaymentReminderWorker.KEY_OFFSET_DAYS to (reminder.offsetDays ?: 0),
                    PaymentReminderWorker.KEY_NOTIFICATION_ID to reminder.id.hashCode(),
                ),
            )
            .addTag(WORK_TAG)
            .addTag(workTagForBill(reminder.nop, reminder.taxYear))
            .build()
        workManager.enqueueUniqueWork(reminder.id, ExistingWorkPolicy.REPLACE, work)
    }

    private suspend fun saveReminders(reminders: List<PaymentReminder>) {
        val records = reminders.map { it.toRecord() }
        dataStore.edit { preferences ->
            preferences[Keys.REMINDERS_JSON] = json.encodeToString(
                ListSerializer(ReminderRecord.serializer()),
                records,
            )
        }
    }

    private fun String?.decodeReminderRecords(): List<PaymentReminder> {
        if (isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(ReminderRecord.serializer()), this)
                .mapNotNull { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    private fun PaymentReminder.toRecord(): ReminderRecord {
        return ReminderRecord(
            id = id,
            nopDisplay = nop.asDisplayText(),
            taxYear = taxYear,
            amount = amount,
            dueDateEpochDay = dueDate?.toEpochDay(),
            offsetDays = offsetDays,
            scheduledAtEpochMillis = scheduledAtEpochMillis,
            status = status.name,
            note = note,
        )
    }

    private fun ReminderRecord.toDomain(): PaymentReminder? {
        val nop = Nop.parseOrNull(nopDisplay) ?: return null
        val status = runCatching { ReminderStatus.valueOf(status) }.getOrNull()
            ?: ReminderStatus.UNAVAILABLE
        return PaymentReminder(
            id = id,
            nop = nop,
            taxYear = taxYear,
            amount = amount,
            dueDate = dueDateEpochDay?.let(LocalDate::ofEpochDay),
            offsetDays = offsetDays,
            scheduledAtEpochMillis = scheduledAtEpochMillis,
            status = status,
            note = note,
        )
    }

    private fun workTagForBill(nop: Nop, taxYear: Int): String {
        return "$WORK_TAG:${nop.asDisplayText()}:$taxYear"
    }

    private object Keys {
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDERS_JSON = stringPreferencesKey("reminders_json")
    }

    @Serializable
    private data class ReminderRecord(
        val id: String,
        val nopDisplay: String,
        val taxYear: Int,
        val amount: Double?,
        val dueDateEpochDay: Long?,
        val offsetDays: Int?,
        val scheduledAtEpochMillis: Long?,
        val status: String,
        val note: String,
    )

    companion object {
        private val REMINDER_OFFSETS = listOf(30, 7, 1)
        private const val WORK_TAG = "pbbku_payment_reminder"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
