package id.pbbku.mobileportal.feature.building

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.BuildingDetail
import id.pbbku.mobileportal.domain.model.BuildingFacility
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PageHeader

@Composable
fun BuildingDetailScreen(
    nopDisplay: String,
    noBng: String,
    onBack: () -> Unit,
    onOpenReport: (String, String) -> Unit,
    viewModel: BuildingDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(nopDisplay, noBng) {
        viewModel.load(nopDisplay, noBng)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                OutlinedButton(onClick = onBack) {
                    Text("Kembali")
                }
                InfoPill(
                    text = "Detail LSPOP",
                    containerColor = MaterialTheme.colorScheme.surface,
                )
                PageHeader(
                    title = "Detail Bangunan",
                    subtitle = "Periksa data bangunan sebelum membuat laporan perubahan.",
                )
                uiState.nop?.let {
                    Text(
                        text = it.asGroupedText(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        item {
            BuildingDetailStatus(
                uiState = uiState,
                onRetry = viewModel::retry,
            )
        }

        uiState.detail?.let { detail ->
            item {
                BuildingDetailCard(
                    detail = detail,
                    onOpenReport = onOpenReport,
                )
            }
            item {
                Text(
                    text = "Fasilitas",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            uiState.facilitiesMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            items(
                items = uiState.facilities,
                key = { "${it.name}-${it.quantity}-${it.unit}" },
            ) { facility ->
                FacilityCard(facility)
            }
        }
    }
}

@Composable
private fun BuildingDetailStatus(
    uiState: BuildingDetailUiState,
    onRetry: () -> Unit,
) {
    when {
        uiState.isLoading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator()
                Text("Memuat detail bangunan...")
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
private fun BuildingDetailCard(
    detail: BuildingDetail,
    onOpenReport: (String, String) -> Unit,
) {
    DetailCard(title = "Data Bangunan ${detail.noBng}") {
        DetailRow("JPB", detail.jpb)
        DetailRow("Jenis bangunan", detail.jenisBangunan)
        DetailRow("Luas bangunan", detail.luasBangunan?.let { "${it.toLong()} m2" })
        DetailRow("Jumlah lantai", detail.jumlahLantai?.toString())
        DetailRow("Tahun dibangun", detail.tahunDibangun?.toString())
        DetailRow("Tahun renovasi", detail.tahunRenovasi?.toString())
        DetailRow("Kondisi", detail.kondisi)
        DetailRow("Konstruksi", detail.konstruksi)
        DetailRow("Atap", detail.atap)
        DetailRow("Dinding", detail.dinding)
        DetailRow("Lantai", detail.lantai)
        DetailRow("Langit-langit", detail.langitLangit)
        DetailRow("NJOP bangunan", detail.nilaiSistemBangunan?.toRupiahText())
        Button(
            onClick = { onOpenReport(detail.nop.asDisplayText(), detail.noBng) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Buat Laporan Perubahan")
        }
    }
}

@Composable
private fun FacilityCard(facility: BuildingFacility) {
    DetailCard(title = facility.name) {
        DetailRow(
            label = "Jumlah",
            value = facility.quantity?.let { quantity ->
                val unit = facility.unit?.let { " $it" }.orEmpty()
                "${quantity.toLong()}$unit"
            },
        )
        DetailRow("Keterangan", facility.description)
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
