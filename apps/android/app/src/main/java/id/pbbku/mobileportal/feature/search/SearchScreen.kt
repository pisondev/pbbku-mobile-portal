package id.pbbku.mobileportal.feature.search

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.ObjekPajakSummary

@Composable
fun SearchScreen(
    onOpenDetail: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Cari Objek Pajak",
                    style = MaterialTheme.typography.headlineSmall,
                )
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("NOP atau nama wajib pajak") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    supportingText = { Text("Pencarian otomatis setelah minimal 3 karakter.") },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { viewModel.loadDemoList(reset = true) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Daftar demo")
                    }
                    OutlinedButton(
                        onClick = viewModel::retry,
                        enabled = uiState.errorMessage != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        item {
            SearchStatus(uiState)
        }

        items(
            items = uiState.results,
            key = { it.nopDisplay },
        ) { result ->
            SearchResultCard(
                result = result,
                onClick = { onOpenDetail(result.nopDisplay) },
            )
        }

        if (uiState.canLoadMore) {
            item {
                Button(
                    onClick = { viewModel.loadDemoList(reset = false) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Muat berikutnya")
                }
            }
        }
    }
}

@Composable
private fun SearchStatus(uiState: SearchUiState) {
    when {
        uiState.isLoading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator()
                Text("Memuat data...")
            }
        }

        uiState.errorMessage != null -> {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        uiState.emptyMessage != null -> {
            Text(
                text = uiState.emptyMessage,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        uiState.results.isNotEmpty() -> {
            val totalText = uiState.totalRows?.let { " dari $it" }.orEmpty()
            Text(
                text = "${uiState.modeLabel}: ${uiState.results.size}$totalText hasil",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    result: ObjekPajakSummary,
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
                text = result.namaWajibPajak ?: "Nama WP tidak tersedia",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "NOP: ${result.nop.asGroupedText()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = result.alamatObjekPajak ?: "Alamat objek tidak tersedia",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            result.njopBumi?.let {
                Text(
                    text = "NJOP bumi: ${it.toRupiahText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
