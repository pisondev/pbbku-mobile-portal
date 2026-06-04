package id.pbbku.mobileportal.feature.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.R
import id.pbbku.mobileportal.core.format.toIndonesianDateText
import id.pbbku.mobileportal.core.format.toIndonesianDateTimeText
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.PaymentReminder
import id.pbbku.mobileportal.domain.model.ReminderStatus
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PrimaryGradientCard
import id.pbbku.mobileportal.ui.component.SectionTitleWithIcon

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
            PrimaryGradientCard {
                InfoPill(
                    text = if (uiState.reminderEnabled) "Reminder aktif" else "Reminder nonaktif",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
                SectionTitleWithIcon(
                    title = "Notifikasi",
                    iconRes = R.drawable.ic_nav_notifications,
                    contentColor = Color.White,
                )
                Text(
                    text = "Daftar pengingat lokal untuk jatuh tempo SPPT.",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = if (uiState.reminderEnabled) {
                        "Pengingat lokal aktif."
                    } else {
                        "Pengingat lokal nonaktif. Aktifkan dari Pengaturan."
                    },
                    color = Color.White.copy(alpha = 0.82f),
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
                    text = "Belum Ada Reminder",
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
    var expanded by remember(reminder.id) { mutableStateOf(false) }
    val containerColor by animateColorAsState(
        targetValue = if (expanded) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "reminder-card-color",
    )
    AppCard(
        modifier = Modifier
            .animateContentSize()
            .clickable { expanded = !expanded },
        containerColor = containerColor,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            InfoPill(
                text = reminder.status.toNotificationLabel(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Tagihan ${reminder.taxYear}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = reminder.amount?.toRupiahText() ?: "Nominal belum tersedia",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = reminder.dueDate?.toIndonesianDateText() ?: "Jatuh tempo belum tersedia",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = if (expanded) "Sembunyikan" else "Lihat Detail",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically() + slideInVertically { -it / 4 },
            exit = fadeOut() + shrinkVertically() + slideOutVertically { -it / 4 },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailText("NOP", reminder.nop.asGroupedText())
                DetailText("Status reminder", reminder.status.toNotificationLabel())
                DetailText("Jatuh tempo", reminder.dueDate?.toIndonesianDateText() ?: "Data tidak tersedia")
                DetailText("Jadwal", reminder.scheduledAtEpochMillis?.toIndonesianDateTimeText() ?: "Data tidak tersedia")
                DetailText("Nominal", reminder.amount?.toRupiahText() ?: "Data tidak tersedia")
                Text(
                    text = reminder.note,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun ReminderStatus.toNotificationLabel(): String {
    return when (this) {
        ReminderStatus.SCHEDULED -> "Terjadwal"
        ReminderStatus.ACTIVE -> "Aktif"
        ReminderStatus.UNAVAILABLE -> "Menunggu Data SPPT"
        ReminderStatus.SIMULATION -> "Prioritas Tagihan"
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
