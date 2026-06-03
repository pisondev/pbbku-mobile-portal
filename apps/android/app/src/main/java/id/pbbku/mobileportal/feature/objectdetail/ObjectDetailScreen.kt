package id.pbbku.mobileportal.feature.objectdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.ObjekPajakDetail
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PageHeader
import id.pbbku.mobileportal.ui.tutorial.TutorialOverlay
import id.pbbku.mobileportal.ui.tutorial.TutorialStep
import id.pbbku.mobileportal.ui.tutorial.TutorialTargetState
import id.pbbku.mobileportal.ui.tutorial.rememberTutorialTargetState
import id.pbbku.mobileportal.ui.tutorial.tutorialTarget

@Composable
fun ObjectDetailScreen(
    nopDisplay: String,
    onBack: () -> Unit,
    onOpenBuilding: (String) -> Unit,
    onOpenSpptHistory: (String) -> Unit,
    onOpenTunggakan: (String) -> Unit,
    onOpenReport: (String) -> Unit,
    viewModel: ObjectDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val tutorialTargetState = rememberTutorialTargetState()
    var showTutorial by rememberSaveable { mutableStateOf(true) }
    val tutorialSteps = listOf(
        TutorialStep(
            targetId = "object-info",
            title = "Periksa identitas objek pajak",
            message = "Bagian ini menampilkan NOP, alamat, luas bumi, NJOP, dan status wajib pajak dengan fallback saat field API kosong.",
        ),
        TutorialStep(
            targetId = "shortcut-sppt",
            title = "Lanjut ke histori SPPT",
            message = "Tekan Histori SPPT untuk melihat tagihan per tahun, status bayar, jatuh tempo, dan akses ke informasi pembayaran.",
            actionLabel = "Buka SPPT",
        ),
        TutorialStep(
            targetId = "shortcut-report",
            title = "Siapkan laporan perubahan",
            message = "Fitur laporan hanya membuat draft prototipe lokal. Data resmi SIMPBB tidak diubah dari aplikasi MVP.",
            actionLabel = "Buat Laporan",
        ),
    )

    LaunchedEffect(nopDisplay) {
        viewModel.load(nopDisplay)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                AppCard(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    OutlinedButton(onClick = onBack) {
                        Text("Kembali")
                    }
                    InfoPill(
                        text = "Data resmi SIMPBB",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                    PageHeader(
                        title = "Detail Objek Pajak",
                        subtitle = "Profil objek pajak, subjek pajak, dan shortcut fitur terkait.",
                    )
                    uiState.nop?.let { nop ->
                        Text(
                            text = nop.asGroupedText(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            item {
                DetailStatus(
                    uiState = uiState,
                    onRetry = viewModel::retry,
                )
            }

            uiState.detail?.let { detail ->
                item {
                    if (uiState.isCacheData && uiState.cacheTimestampText != null) {
                        Text(
                            text = "Data cache terakhir diperbarui ${uiState.cacheTimestampText}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                item {
                    ObjectInfoCard(
                        detail = detail,
                        modifier = Modifier.tutorialTarget(tutorialTargetState, "object-info"),
                        onCopyNop = {
                            clipboardManager.setText(AnnotatedString(detail.nopDisplay))
                        },
                    )
                }
                item {
                    SubjectInfoCard(detail = detail)
                }
                item {
                    ShortcutCard(
                        nopDisplay = detail.nopDisplay,
                        tutorialTargetState = tutorialTargetState,
                        onOpenBuilding = onOpenBuilding,
                        onOpenSpptHistory = onOpenSpptHistory,
                        onOpenTunggakan = onOpenTunggakan,
                        onOpenReport = onOpenReport,
                    )
                }
            }
        }
        TutorialOverlay(
            visible = showTutorial && uiState.detail != null,
            steps = tutorialSteps,
            targetState = tutorialTargetState,
            modifier = Modifier.align(Alignment.BottomCenter),
            onDismiss = { showTutorial = false },
        )
    }
}

@Composable
private fun DetailStatus(
    uiState: ObjectDetailUiState,
    onRetry: () -> Unit,
) {
    when {
        uiState.isLoading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator()
                Text("Memuat detail...")
            }
        }

        uiState.errorMessage != null -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = onRetry) {
                    Text("Coba Lagi")
                }
            }
        }

        uiState.emptyMessage != null -> {
            Text(
                text = uiState.emptyMessage,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ObjectInfoCard(
    detail: ObjekPajakDetail,
    modifier: Modifier = Modifier,
    onCopyNop: () -> Unit,
) {
    DetailCard(
        title = "Objek Pajak",
        modifier = modifier,
    ) {
        DetailRow("NOP", detail.nop.asGroupedText())
        DetailRow("Alamat objek", detail.alamatObjekPajak)
        DetailRow("Luas bumi", detail.luasBumi?.let { "${it.toLong()} m2" })
        DetailRow("NJOP bumi", detail.nilaiSistemBumi?.toRupiahText())
        DetailRow("Jenis bumi", detail.jenisBumi)
        DetailRow("Status WP", detail.statusWajibPajak)
        OutlinedButton(
            onClick = onCopyNop,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Salin NOP")
        }
    }
}

@Composable
private fun SubjectInfoCard(detail: ObjekPajakDetail) {
    DetailCard(title = "Subjek Pajak") {
        DetailRow("Nama WP", detail.namaWajibPajak)
        DetailRow("Alamat WP", detail.alamatWajibPajak)
        DetailRow("Pekerjaan", detail.statusPekerjaanWajibPajak)
    }
}

@Composable
private fun ShortcutCard(
    nopDisplay: String,
    tutorialTargetState: TutorialTargetState,
    onOpenBuilding: (String) -> Unit,
    onOpenSpptHistory: (String) -> Unit,
    onOpenTunggakan: (String) -> Unit,
    onOpenReport: (String) -> Unit,
) {
    DetailCard(title = "Menu Terkait") {
        Button(
            onClick = { onOpenBuilding(nopDisplay) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Bangunan")
        }
        OutlinedButton(
            onClick = { onOpenSpptHistory(nopDisplay) },
            modifier = Modifier
                .fillMaxWidth()
                .tutorialTarget(tutorialTargetState, "shortcut-sppt"),
        ) {
            Text("Histori SPPT")
        }
        OutlinedButton(
            onClick = { onOpenTunggakan(nopDisplay) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Tunggakan")
        }
        OutlinedButton(
            onClick = { onOpenReport(nopDisplay) },
            modifier = Modifier
                .fillMaxWidth()
                .tutorialTarget(tutorialTargetState, "shortcut-report"),
        ) {
            Text("Laporan Perubahan")
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "Data tidak tersedia",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
