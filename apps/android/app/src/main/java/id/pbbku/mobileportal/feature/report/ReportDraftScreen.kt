package id.pbbku.mobileportal.feature.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var isReadOnly by remember { mutableStateOf(false) }
    var waitingForSaveFeedback by remember { mutableStateOf(false) }
    var showSaveSuccessPopup by remember { mutableStateOf(false) }

    LaunchedEffect(nopDisplay, noBng) {
        viewModel.load(nopDisplay, noBng)
        isReadOnly = false
        showSaveSuccessPopup = false
        waitingForSaveFeedback = false
    }

    LaunchedEffect(uiState.saveMessage) {
        val message = uiState.saveMessage ?: return@LaunchedEffect
        if (!waitingForSaveFeedback) return@LaunchedEffect
        waitingForSaveFeedback = false
        if (message == "Draft permohonan disimpan.") {
            isReadOnly = true
            showSaveSuccessPopup = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        text = "Draft perubahan",
                        containerColor = MaterialTheme.colorScheme.surface,
                    )
                    PageHeader(
                        title = "Laporan Perubahan Bangunan",
                        subtitle = "Data lama LSPOP dimuat dari SIMPBB OP dan disiapkan sebagai permohonan verifikasi.",
                        iconRes = R.drawable.shortcut_laporan_perubahan,
                        titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Permohonan masuk ke alur verifikasi petugas. Data resmi baru berubah setelah diverifikasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SavedReadonlyBanner(
                    visible = isReadOnly,
                    onEdit = {
                        isReadOnly = false
                        showSaveSuccessPopup = false
                    },
                )
            }
            item {
                ReportStatusCard(uiState)
            }
            item {
                BuildingIdentityForm(
                    uiState = uiState,
                    readOnly = isReadOnly,
                    onNoBngChange = viewModel::onNoBngChange,
                )
            }
            item {
                ChangeTypeSelector(
                    selected = uiState.changeType,
                    readOnly = isReadOnly,
                    onSelected = viewModel::onChangeTypeChange,
                )
            }
            if (uiState.showsAreaFields || uiState.showsFloorFields) {
                item {
                    BuildingComparisonForm(
                        uiState = uiState,
                        readOnly = isReadOnly,
                        onNewBuildingAreaChange = viewModel::onNewBuildingAreaChange,
                        onNewFloorCountChange = viewModel::onNewFloorCountChange,
                    )
                }
            }
            item {
                DescriptionForm(
                    uiState = uiState,
                    readOnly = isReadOnly,
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
                    readOnly = isReadOnly,
                    onEdit = {
                        isReadOnly = false
                        showSaveSuccessPopup = false
                    },
                    onSaveDraft = {
                        waitingForSaveFeedback = true
                        viewModel.saveDraft()
                    },
                    onPrepareSummary = viewModel::prepareSummary,
                    onSendSimulation = viewModel::sendSimulation,
                    onDeleteDraft = {
                        isReadOnly = false
                        showSaveSuccessPopup = false
                        viewModel.deleteDraft()
                    },
                )
            }
        }
        if (showSaveSuccessPopup) {
            AlertDialog(
                onDismissRequest = { showSaveSuccessPopup = false },
                title = { Text("Draft Tersimpan") },
                text = {
                    Text(
                        text = "Draft permohonan tersimpan. Form dikunci agar status tersimpan terlihat jelas. Tekan Edit Draft untuk mengubah isian lagi.",
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showSaveSuccessPopup = false }) {
                        Text("Mengerti")
                    }
                },
            )
        }
    }
}

@Composable
private fun SavedReadonlyBanner(
    visible: Boolean,
    onEdit: () -> Unit,
) {
    AnimatedVisibility(visible = visible) {
        AppCard(
            modifier = Modifier.animateContentSize(),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Draft tersimpan",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Form sedang read-only supaya perubahan yang tersimpan terlihat jelas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(onClick = onEdit) {
                    Text("Edit Draft")
                }
            }
        }
    }
}

@Composable
private fun ReportStatusCard(uiState: ReportDraftUiState) {
    DetailCard(title = "Status Laporan") {
        InfoPill(text = uiState.status.toDisplayText())
        DetailRow("NOP", uiState.nop?.asGroupedText())
        DetailRow("Status", uiState.status.toDisplayText())
        DetailRow("Sumber data", "SIMPBB OP")
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
    readOnly: Boolean,
    onNoBngChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedBuilding = uiState.selectedBuilding
    DetailCard(title = "Data Bangunan dari SIMPBB OP") {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isAccessAllowed && uiState.availableBuildings.isNotEmpty() && !readOnly,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = selectedBuilding?.let {
                        "${it.label} - Nomor LSPOP ${it.noBng}"
                    } ?: "Pilih nomor bangunan",
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                uiState.availableBuildings.forEach { building ->
                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = building.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = "Nomor LSPOP ${building.noBng}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onNoBngChange(building.noBng)
                        },
                    )
                }
            }
        }
        DetailRow("JPB lama", uiState.oldBuildingDetail?.jpb)
        DetailRow("Jenis bangunan lama", uiState.oldBuildingDetail?.jenisBangunan)
    }
}

@Composable
private fun ChangeTypeSelector(
    selected: String,
    readOnly: Boolean,
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
                    enabled = !readOnly,
                ) {
                    Text(option)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !readOnly,
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
    readOnly: Boolean,
    onNewBuildingAreaChange: (String) -> Unit,
    onNewFloorCountChange: (String) -> Unit,
) {
    DetailCard(title = "Perbandingan Data Permohonan") {
        if (uiState.showsAreaFields) {
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
                readOnly = readOnly,
                enabled = !readOnly,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.validation.newBuildingAreaError != null,
                supportingText = {
                    uiState.validation.newBuildingAreaError?.let { Text(it) }
                },
            )
        }
        if (uiState.showsFloorFields) {
            DetailRow("Jumlah lantai lama", uiState.oldFloorCountText.takeIf { it.isNotBlank() })
            OutlinedTextField(
                value = uiState.newFloorCountText,
                onValueChange = onNewFloorCountChange,
                label = { Text("Jumlah lantai baru") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = readOnly,
                enabled = !readOnly,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.validation.newFloorCountError != null,
                supportingText = {
                    uiState.validation.newFloorCountError?.let { Text(it) }
                },
            )
        }
    }
}

@Composable
private fun DescriptionForm(
    uiState: ReportDraftUiState,
    readOnly: Boolean,
    onDescriptionChange: (String) -> Unit,
) {
    DetailCard(title = "Deskripsi Perubahan") {
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Deskripsi") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            readOnly = readOnly,
            enabled = !readOnly,
            isError = uiState.validation.descriptionError != null,
            supportingText = {
                Text(uiState.validation.descriptionError ?: "Wajib diisi sebelum pengajuan.")
            },
        )
    }
}

@Composable
private fun ReportSummaryCard(uiState: ReportDraftUiState) {
    DetailCard(title = "Ringkasan Permohonan") {
        DetailRow("NOP", uiState.nop?.asGroupedText())
        DetailRow("Bangunan", uiState.noBng.takeIf { it.isNotBlank() })
        DetailRow("Jenis perubahan", uiState.changeType)
        if (uiState.showsAreaFields) {
            DetailRow("Luas lama", uiState.oldBuildingAreaText.takeIf { it.isNotBlank() }?.let { "$it m2" })
            DetailRow("Luas baru", uiState.newBuildingAreaText.takeIf { it.isNotBlank() }?.let { "$it m2" })
        }
        if (uiState.showsFloorFields) {
            DetailRow("Lantai lama", uiState.oldFloorCountText.takeIf { it.isNotBlank() })
            DetailRow("Lantai baru", uiState.newFloorCountText.takeIf { it.isNotBlank() })
        }
        DetailRow("Deskripsi", uiState.description)
        Text(
            text = "Ringkasan ini disiapkan sebagai berkas permohonan verifikasi perubahan bangunan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionButtons(
    uiState: ReportDraftUiState,
    readOnly: Boolean,
    onEdit: () -> Unit,
    onSaveDraft: () -> Unit,
    onPrepareSummary: () -> Unit,
    onSendSimulation: () -> Unit,
    onDeleteDraft: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = "Simpan Draft mengunci isian sebagai konsep permohonan yang masih bisa diedit. Kirim Permohonan Verifikasi mengubah status menjadi menunggu verifikasi petugas; data resmi SIMPBB tetap menunggu proses validasi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(
            onClick = onPrepareSummary,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isAccessAllowed,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text("Tampilkan Ringkasan")
        }
        if (readOnly) {
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Edit Draft")
            }
        }
        Button(
            onClick = onSaveDraft,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isAccessAllowed && !readOnly,
        ) {
            Text("Simpan Draft")
        }
        OutlinedButton(
            onClick = onSendSimulation,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isAccessAllowed,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text("Kirim Permohonan Verifikasi")
        }
        Button(
            onClick = onDeleteDraft,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isAccessAllowed && !uiState.isDeleting,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
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
        ReportDraftStatus.SENT_SIMULATION -> "Menunggu Verifikasi"
    }
}
