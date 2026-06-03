package id.pbbku.mobileportal.feature.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.core.format.toIndonesianDateText
import id.pbbku.mobileportal.core.format.toIndonesianDateTimeText
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.PaymentReminder
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PageHeader

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.reminders.isEmpty()) {
        if (uiState.reminders.isEmpty()) viewModel.showDemoReminder()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            AppCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                InfoPill(
                    text = if (uiState.reminderEnabled) "Reminder aktif" else "Reminder nonaktif",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
                PageHeader(
                    title = "Notifikasi",
                    subtitle = "Daftar reminder lokal untuk jatuh tempo SPPT.",
                )
                Text(
                    text = if (uiState.reminderEnabled) {
                        "Pengingat lokal aktif."
                    } else {
                        "Pengingat lokal nonaktif. Aktifkan dari Pengaturan."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (uiState.reminders.isEmpty()) {
            item {
                EmptyCard()
            }
        }

        items(
            items = uiState.reminders,
            key = { it.id },
        ) { reminder ->
            ReminderCard(reminder)
        }
    }
}

@Composable
private fun EmptyCard() {
    AppCard {
            Text(
                text = "Belum ada reminder",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Reminder akan muncul setelah histori SPPT atau tunggakan dengan tanggal jatuh tempo tersedia.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
}

@Composable
private fun ReminderCard(reminder: PaymentReminder) {
    AppCard {
            InfoPill(
                text = reminder.status.displayText,
                containerColor = if (reminder.isSimulation) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                contentColor = if (reminder.isSimulation) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
            )
            Text(
                text = "Tagihan ${reminder.taxYear}",
                style = MaterialTheme.typography.titleMedium,
            )
            DetailText("NOP", reminder.nop.asGroupedText())
            DetailText("Status reminder", reminder.status.displayText)
            DetailText("Jatuh tempo", reminder.dueDate?.toIndonesianDateText() ?: "Data tidak tersedia")
            DetailText("Jadwal", reminder.scheduledAtEpochMillis?.toIndonesianDateTimeText() ?: "Data tidak tersedia")
            DetailText("Nominal", reminder.amount?.toRupiahText() ?: "Data tidak tersedia")
            Text(
                text = reminder.note,
                color = if (reminder.isSimulation) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
}

@Composable
private fun DetailText(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
