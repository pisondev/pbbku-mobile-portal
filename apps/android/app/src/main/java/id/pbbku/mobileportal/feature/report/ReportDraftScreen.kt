package id.pbbku.mobileportal.feature.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.R
import id.pbbku.mobileportal.data.local.report.ReportDraftStatus
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.LoadingSkeletonCard
import id.pbbku.mobileportal.ui.component.PageHeader

@Composable
fun ReportDraftScreen(
    nopDisplay: String,
    noBng: String?,
    onBack: () -> Unit,
    viewModel: ReportDraftViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(nopDisplay, noBng) {
        viewModel.load(nopDisplay, noBng)
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
                OutlinedButton(onClick = onBack) {
                    Text("Kembali")
                }
                InfoPill(
                    text = "Draft perubahan",
                    containerColor = MaterialTheme.colorScheme.surface,
                )
                PageHeader(
                    title = "Laporan Perubahan Bangunan",
                    subtitle = "Susun laporan perubahan LSPOP tanpa mengubah data resmi SIMPBB.",
                    iconRes = R.drawable.shortcut_laporan_perubahan,
                    titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Perubahan data resmi tetap memerlukan verifikasi petugas Bapenda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            ReportStatusCard(uiState)
        }
        item {
            BuildingIdentityForm(
                uiState = uiState,
                onNoBngChange = viewModel::onNoBngChange,
            )
        }
        item {
            ChangeTypeSelector(
                selected = uiState.changeType,
                onSelected = viewModel::onChangeTypeChange,
            )
        }
        item {
            BuildingComparisonForm(
                uiState = uiState,
                onNewBuildingAreaChange = viewModel::onNewBuildingAreaChange,
                onNewFloorCountChange = viewModel::onNewFloorCountChange,
            )
        }
        item {
            DescriptionForm(
                uiState = uiState,
                onDescriptionChange = viewModel::onDescriptionChange,
            )
        }
        if (uiState.showSummary) {
            item {
                ReportSummaryCard(uiState)
            }
        }
        item {
            ActionButtons(
                uiState = uiState,
                onSaveDraft = viewModel::saveDraft,
                onPrepareSummary = viewModel::prepareSummary,
                onSendSimulation = viewModel::sendSimulation,
                onDeleteDraft = viewModel::deleteDraft,
            )
        }
    }
}

@Composable
private fun ReportStatusCard(uiState: ReportDraftUiState) {
    DetailCard(title = "Status Laporan") {
        InfoPill(text = uiState.status.toDisplayText())
        DetailRow("NOP", uiState.nop?.asGroupedText())
        DetailRow("Status", uiState.status.toDisplayText())
        if (uiState.isLoadingBuilding) {
            LoadingSkeletonCard()
        }
        uiState.buildingMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        uiState.saveMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun BuildingIdentityForm(
    uiState: ReportDraftUiState,
    onNoBngChange: (String) -> Unit,
) {
    DetailCard(title = "Identitas Bangunan") {
        OutlinedTextField(
            value = uiState.noBng,
            onValueChange = onNoBngChange,
            label = { Text("Nomor bangunan") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        DetailRow("JPB lama", uiState.oldBuildingDetail?.jpb)
        DetailRow("Jenis bangunan lama", uiState.oldBuildingDetail?.jenisBangunan)
    }
}

@Composable
private fun ChangeTypeSelector(
    selected: String,
    onSelected: (String) -> Unit,
) {
    val options = listOf(
        "Perubahan luas bangunan",
        "Perubahan jumlah lantai",
        "Perubahan data bangunan",
    )
    DetailCard(title = "Jenis Perubahan") {
        options.forEach { option ->
            val isSelected = selected == option
            if (isSelected) {
                Button(
                    onClick = { onSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(option)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(option)
                }
            }
        }
    }
}

@Composable
private fun BuildingComparisonForm(
    uiState: ReportDraftUiState,
    onNewBuildingAreaChange: (String) -> Unit,
    onNewFloorCountChange: (String) -> Unit,
) {
    DetailCard(title = "Perbandingan Data") {
        DetailRow(
            label = "Luas bangunan lama",
            value = uiState.oldBuildingAreaText.takeIf { it.isNotBlank() }?.let { "$it m2" },
        )
        OutlinedTextField(
            value = uiState.newBuildingAreaText,
            onValueChange = onNewBuildingAreaChange,
            label = { Text("Luas bangunan baru (m2)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = uiState.validation.newBuildingAreaError != null,
            supportingText = {
                uiState.validation.newBuildingAreaError?.let { Text(it) }
            },
        )
        DetailRow("Jumlah lantai lama", uiState.oldFloorCountText.takeIf { it.isNotBlank() })
        OutlinedTextField(
            value = uiState.newFloorCountText,
            onValueChange = onNewFloorCountChange,
            label = { Text("Jumlah lantai baru") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = uiState.validation.newFloorCountError != null,
            supportingText = {
                uiState.validation.newFloorCountError?.let { Text(it) }
            },
        )
    }
}

@Composable
private fun DescriptionForm(
    uiState: ReportDraftUiState,
    onDescriptionChange: (String) -> Unit,
) {
    DetailCard(title = "Deskripsi Perubahan") {
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Deskripsi") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            isError = uiState.validation.descriptionError != null,
            supportingText = {
                Text(uiState.validation.descriptionError ?: "Wajib diisi sebelum pengajuan.")
            },
        )
    }
}

@Composable
private fun ReportSummaryCard(uiState: ReportDraftUiState) {
    DetailCard(title = "Ringkasan Sebelum Pengajuan") {
        DetailRow("NOP", uiState.nop?.asGroupedText())
        DetailRow("Bangunan", uiState.noBng.takeIf { it.isNotBlank() })
        DetailRow("Jenis perubahan", uiState.changeType)
        DetailRow("Luas lama", uiState.oldBuildingAreaText.takeIf { it.isNotBlank() }?.let { "$it m2" })
        DetailRow("Luas baru", uiState.newBuildingAreaText.takeIf { it.isNotBlank() }?.let { "$it m2" })
        DetailRow("Lantai lama", uiState.oldFloorCountText.takeIf { it.isNotBlank() })
        DetailRow("Lantai baru", uiState.newFloorCountText.takeIf { it.isNotBlank() })
        DetailRow("Deskripsi", uiState.description)
        Text(
            text = "Ringkasan ini belum dikirim ke petugas dan masih tersimpan di perangkat.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionButtons(
    uiState: ReportDraftUiState,
    onSaveDraft: () -> Unit,
    onPrepareSummary: () -> Unit,
    onSendSimulation: () -> Unit,
    onDeleteDraft: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onSaveDraft,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Simpan Draft")
        }
        OutlinedButton(
            onClick = onPrepareSummary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Tampilkan Ringkasan")
        }
        OutlinedButton(
            onClick = onSendSimulation,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Tandai Sudah Diajukan")
        }
        OutlinedButton(
            onClick = onDeleteDraft,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isDeleting,
        ) {
            Text("Hapus Draft")
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
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

private fun ReportDraftStatus.toDisplayText(): String {
    return when (this) {
        ReportDraftStatus.DRAFT -> "Draft"
        ReportDraftStatus.READY_TO_SUBMIT -> "Siap Diajukan"
        ReportDraftStatus.SENT_SIMULATION -> "Sudah Diajukan"
    }
}
