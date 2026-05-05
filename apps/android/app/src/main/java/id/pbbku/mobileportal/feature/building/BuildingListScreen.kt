package id.pbbku.mobileportal.feature.building

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.BuildingSummary

@Composable
fun BuildingListScreen(
    nopDisplay: String,
    onBack: () -> Unit,
    onOpenDetail: (String, String) -> Unit,
    viewModel: BuildingListViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(nopDisplay) {
        viewModel.load(nopDisplay)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack) {
                    Text("Kembali")
                }
                Text(
                    text = "Daftar Bangunan",
                    style = MaterialTheme.typography.headlineSmall,
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
            BuildingListStatus(
                uiState = uiState,
                onRetry = viewModel::retry,
            )
        }

        items(
            items = uiState.buildings,
            key = { it.noBng },
        ) { building ->
            BuildingSummaryCard(
                building = building,
                onClick = { onOpenDetail(building.nop.asDisplayText(), building.noBng) },
            )
        }
    }
}

@Composable
private fun BuildingListStatus(
    uiState: BuildingListUiState,
    onRetry: () -> Unit,
) {
    when {
        uiState.isLoading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator()
                Text("Memuat bangunan...")
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

        uiState.buildings.isNotEmpty() -> {
            Text(
                text = "${uiState.buildings.size} bangunan ditemukan.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun BuildingSummaryCard(
    building: BuildingSummary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = building.label,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = building.jpb ?: building.jenisBangunan ?: "Jenis bangunan tidak tersedia",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Luas: ${building.luasBangunan?.let { "${it.toLong()} m2" } ?: "Data tidak tersedia"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Lantai: ${building.jumlahLantai?.toString() ?: "Data tidak tersedia"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            building.nilaiSistemBangunan?.let {
                Text(
                    text = "NJOP bangunan: ${it.toRupiahText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
