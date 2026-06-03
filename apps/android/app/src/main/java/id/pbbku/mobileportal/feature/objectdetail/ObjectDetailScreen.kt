package id.pbbku.mobileportal.feature.objectdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.R
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.ObjekPajakDetail
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.LoadingSkeletonCard
import id.pbbku.mobileportal.ui.component.PageHelpButton
import id.pbbku.mobileportal.ui.component.PrimaryGradientCard
import id.pbbku.mobileportal.ui.component.SectionTitleWithIcon
import id.pbbku.mobileportal.ui.component.ShortcutMenuCard
import id.pbbku.mobileportal.ui.component.ShortcutMenuItem
import id.pbbku.mobileportal.ui.tutorial.TutorialOverlay
import id.pbbku.mobileportal.ui.tutorial.TutorialStep
import id.pbbku.mobileportal.ui.tutorial.TutorialTargetState
import id.pbbku.mobileportal.ui.tutorial.rememberTutorialVisibilityState
import id.pbbku.mobileportal.ui.tutorial.rememberTutorialTargetState
import id.pbbku.mobileportal.ui.tutorial.tutorialTarget

@Composable
fun ObjectDetailScreen(
    nopDisplay: String,
    helpRequestId: Int,
    onRequestHelp: () -> Unit,
    onBack: () -> Unit,
    onOpenBuilding: (String) -> Unit,
    onOpenSpptHistory: (String) -> Unit,
    onOpenTunggakan: (String) -> Unit,
    onOpenReport: (String) -> Unit,
    viewModel: ObjectDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val tutorialTargetState = rememberTutorialTargetState()
    val tutorialVisibility = rememberTutorialVisibilityState(
        pageKey = "object_detail",
        helpRequestId = helpRequestId,
    )
    val tutorialSteps = listOf(
        TutorialStep(
            targetId = "object-info",
            title = "Periksa identitas objek pajak",
            message = "Bagian ini menampilkan NOP, alamat, luas bumi, NJOP, dan status wajib pajak bila datanya tersedia.",
        ),
        TutorialStep(
            targetId = "object-shortcuts",
            title = "Lanjut ke histori SPPT",
            message = "Tekan Histori SPPT untuk melihat tagihan per tahun, status bayar, jatuh tempo, dan akses ke informasi pembayaran.",
            actionLabel = "Buka SPPT",
        ),
        TutorialStep(
            targetId = "object-shortcuts",
            title = "Siapkan laporan perubahan",
            message = "Fitur laporan membantu menyiapkan draft perubahan tanpa langsung mengubah data resmi SIMPBB.",
            actionLabel = "Buat Laporan",
        ),
    )

    LaunchedEffect(nopDisplay) {
        viewModel.load(nopDisplay)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                PrimaryGradientCard {
                    InfoPill(
                        text = "Data resmi SIMPBB",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                    SectionTitleWithIcon(
                        title = "Detail Objek",
                        iconRes = R.drawable.ic_nav_search,
                        contentColor = Color.White,
                    )
                    Text(
                        text = "Profil subjek, data objek, dan fitur lanjutan untuk NOP yang dipilih.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                    uiState.nop?.let { nop ->
                        Text(
                            text = nop.asGroupedText(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
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
                    SubjectInfoCard(detail = detail)
                }
                item {
                    ObjectInfoCard(
                        detail = detail,
                        modifier = Modifier.tutorialTarget(tutorialTargetState, "object-info"),
                    )
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
            visible = tutorialVisibility.visible && uiState.detail != null,
            steps = tutorialSteps,
            targetState = tutorialTargetState,
            modifier = Modifier.align(Alignment.BottomCenter),
            onDismiss = tutorialVisibility.dismiss,
        )
        PageHelpButton(
            onClick = onRequestHelp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 28.dp),
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
            LoadingSkeletonCard()
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
) {
    DetailCard(
        title = "Objek Pajak",
        modifier = modifier,
    ) {
        DetailRow(
            label = "NOP",
            value = detail.nop.asGroupedText(),
        )
        DetailRow("Alamat objek", detail.alamatObjekPajak)
        DetailRow("Luas bumi", detail.luasBumi?.let { "${it.toLong()} m2" })
        DetailRow("NJOP bumi", detail.nilaiSistemBumi?.toRupiahText())
        DetailRow("Jenis bumi", detail.jenisBumi)
        DetailRow("Status WP", detail.statusWajibPajak)
    }
}

@Composable
private fun SubjectInfoCard(detail: ObjekPajakDetail) {
    DetailCard(title = "Subjek Pajak") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFF2DD4BF),
                            ),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "WP",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = detail.namaWajibPajak?.takeIf { it.isNotBlank() }
                        ?: "Nama WP tidak tersedia",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Profil wajib pajak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
    ShortcutMenuCard(
        title = "Menu Terkait",
        subtitle = "Pilih fitur lanjutan untuk objek pajak ini.",
        columns = 2,
        tileMinHeightDp = 108,
        modifier = Modifier.tutorialTarget(tutorialTargetState, "object-shortcuts"),
        items = listOf(
            ShortcutMenuItem(
                title = "Bangunan",
                description = "Data bangunan",
                iconRes = R.drawable.shortcut_bangunan,
                onClick = { onOpenBuilding(nopDisplay) },
            ),
            ShortcutMenuItem(
                title = "Histori SPPT",
                description = "Tagihan tahunan",
                iconRes = R.drawable.shortcut_histori_sppt,
                onClick = { onOpenSpptHistory(nopDisplay) },
            ),
            ShortcutMenuItem(
                title = "Tunggakan",
                description = "Belum lunas",
                iconRes = R.drawable.shortcut_tunggakan,
                onClick = { onOpenTunggakan(nopDisplay) },
            ),
            ShortcutMenuItem(
                title = "Laporan",
                description = "Perubahan data",
                iconRes = R.drawable.shortcut_laporan_perubahan,
                onClick = { onOpenReport(nopDisplay) },
            ),
        ),
    )
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
    copyable: Boolean = true,
) {
    val clipboardManager = LocalClipboardManager.current
    val displayValue = value?.takeIf { it.isNotBlank() } ?: "Data tidak tersedia"
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            shape = MaterialTheme.shapes.small,
            color = Color(0xFFE0E7F0),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp)
                    .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayValue,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (copyable && !value.isNullOrBlank()) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(displayValue))
                        },
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_copy),
                            contentDescription = "Salin $label",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
