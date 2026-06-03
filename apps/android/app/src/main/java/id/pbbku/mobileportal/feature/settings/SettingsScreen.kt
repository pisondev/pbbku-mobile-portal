package id.pbbku.mobileportal.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.R
import id.pbbku.mobileportal.data.session.SimulatedSession
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PrimaryGradientCard
import id.pbbku.mobileportal.ui.component.SectionTitleWithIcon

@Composable
fun SettingsScreen(
    session: SimulatedSession?,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.setReminderEnabled(true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            PrimaryGradientCard {
                InfoPill(
                    text = "Kontrol data lokal",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
                SectionTitleWithIcon(
                    title = "Pengaturan",
                    iconRes = R.drawable.ic_nav_settings,
                    contentColor = Color.White,
                )
                Text(
                    text = "Kelola pengingat, cache, draft laporan, informasi aplikasi, dan sesi pengguna.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
        }
        item {
            ReminderCard(
                reminderEnabled = reminderEnabled,
                onReminderChanged = { checked ->
                    if (
                        checked &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setReminderEnabled(checked)
                    }
                },
            )
        }
        item {
            LocalDataCard(
                title = "Cache Data Terakhir",
                description = "Menghapus cache read-only yang dipakai saat data detail berhasil dimuat.",
                actionText = "Hapus Cache",
                message = uiState.cacheMessage,
                onAction = viewModel::clearCache,
            )
        }
        item {
            LocalDataCard(
                title = "Draft Laporan",
                description = "Menghapus semua draft laporan perubahan bangunan yang tersimpan lokal.",
                actionText = "Hapus Draft Laporan",
                message = uiState.draftMessage,
                onAction = viewModel::clearDrafts,
            )
        }
        item {
            AppInfoCard(uiState.appVersionText)
        }
        if (uiState.debugModeEnabled) {
            item {
                DebugInfoCard(viewModel.debugSummary())
            }
        }
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Keluar")
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminderEnabled: Boolean,
    onReminderChanged: (Boolean) -> Unit,
) {
    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Pengingat Jatuh Tempo",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Jadwalkan reminder lokal 30, 7, dan 1 hari sebelum jatuh tempo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = reminderEnabled,
                onCheckedChange = onReminderChanged,
            )
        }
    }
}

@Composable
private fun LocalDataCard(
    title: String,
    description: String,
    actionText: String,
    message: String?,
    onAction: () -> Unit,
) {
    AppCard {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(actionText)
            }
            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
}

@Composable
private fun AppInfoCard(versionText: String) {
    AppCard {
            Text(
                text = "Informasi Aplikasi",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = versionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Aplikasi Android portal wajib pajak PBB-P2.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
}

@Composable
private fun DebugInfoCard(items: List<String>) {
    AppCard(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
            Text(
                text = "Mode Pengembang",
                style = MaterialTheme.typography.bodyLarge,
            )
            items.forEach { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
}
