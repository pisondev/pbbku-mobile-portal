package id.pbbku.mobileportal.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.ObjekPajakSummary
import id.pbbku.mobileportal.domain.model.WilayahItem
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PageHeader
import id.pbbku.mobileportal.ui.component.StateCard
import id.pbbku.mobileportal.ui.tutorial.TutorialOverlay
import id.pbbku.mobileportal.ui.tutorial.TutorialStep
import id.pbbku.mobileportal.ui.tutorial.rememberTutorialTargetState
import id.pbbku.mobileportal.ui.tutorial.tutorialTarget

@Composable
fun SearchScreen(
    onOpenDetail: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val tutorialTargetState = rememberTutorialTargetState()
    var showTutorial by rememberSaveable { mutableStateOf(true) }
    val tutorialSteps = listOf(
        TutorialStep(
            targetId = "search-input",
            title = "Ketik NOP atau nama wajib pajak",
            message = "Input minimal 3 karakter. Sistem akan mencari lewat endpoint objekPajak/search dengan wrapper json.",
        ),
        TutorialStep(
            targetId = "demo-list",
            title = "Gunakan daftar demo bila perlu",
            message = "Tombol ini memuat daftar objek pajak demo dengan pagination, berguna saat reviewer ingin melihat variasi data.",
            actionLabel = "Muat Demo",
        ),
        TutorialStep(
            targetId = "search-result",
            title = "Pilih hasil untuk lanjut",
            message = "Setelah hasil muncul, tekan salah satu kartu untuk membuka detail objek pajak, lalu lanjut ke bangunan, SPPT, tunggakan, atau laporan.",
        ),
    )

    LaunchedEffect(Unit) {
        viewModel.loadPropinsiIfNeeded()
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
                        text = "Pencarian SIMPBB",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                    PageHeader(
                        title = "Cari Objek Pajak",
                        subtitle = "Cari NOP atau nama wajib pajak. Hasil detail tetap dibaca dari data resmi SIMPBB OP API.",
                    )
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .tutorialTarget(tutorialTargetState, "search-input"),
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
                            modifier = Modifier
                                .weight(1f)
                                .tutorialTarget(tutorialTargetState, "demo-list"),
                        ) {
                            Text("Daftar demo")
                        }
                        OutlinedButton(
                            onClick = viewModel::retry,
                            enabled = uiState.errorMessage != null,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }

            item {
                WilayahFilter(
                    filter = uiState.wilayahFilter,
                    onSelectPropinsi = viewModel::selectPropinsi,
                    onSelectDati2 = viewModel::selectDati2,
                    onSelectKecamatan = viewModel::selectKecamatan,
                    onSelectKelurahan = viewModel::selectKelurahan,
                    onSelectBlok = viewModel::selectBlok,
                    onClear = viewModel::clearWilayahFilter,
                )
            }

            item {
                SearchStatus(
                    uiState = uiState,
                    onRetry = viewModel::retry,
                )
            }

            itemsIndexed(
                items = uiState.results,
                key = { _, item -> item.nopDisplay },
            ) { index, result ->
                val targetModifier = if (index == 0) {
                    Modifier.tutorialTarget(tutorialTargetState, "search-result")
                } else {
                    Modifier
                }
                SearchResultCard(
                    result = result,
                    modifier = targetModifier,
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
        TutorialOverlay(
            visible = showTutorial,
            steps = tutorialSteps,
            targetState = tutorialTargetState,
            modifier = Modifier.align(Alignment.BottomCenter),
            onDismiss = { showTutorial = false },
        )
    }
}

@Composable
private fun WilayahFilter(
    filter: WilayahFilterUiState,
    onSelectPropinsi: (WilayahItem?) -> Unit,
    onSelectDati2: (WilayahItem?) -> Unit,
    onSelectKecamatan: (WilayahItem?) -> Unit,
    onSelectKelurahan: (WilayahItem?) -> Unit,
    onSelectBlok: (WilayahItem?) -> Unit,
    onClear: () -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Filter wilayah",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                )
                OutlinedButton(
                    onClick = onClear,
                    enabled = filter.hasAnySelection,
                ) {
                    Text("Reset")
                }
            }
            WilayahDropdown(
                label = "Provinsi",
                selectedText = filter.selectedPropinsi?.displayText ?: "Pilih provinsi",
                items = filter.propinsi,
                enabled = filter.propinsi.isNotEmpty(),
                onSelected = onSelectPropinsi,
            )
            WilayahDropdown(
                label = "Kabupaten/Kota",
                selectedText = filter.selectedDati2?.displayText ?: "Pilih kabupaten/kota",
                items = filter.dati2,
                enabled = filter.selectedPropinsi != null && filter.dati2.isNotEmpty(),
                onSelected = onSelectDati2,
            )
            WilayahDropdown(
                label = "Kecamatan",
                selectedText = filter.selectedKecamatan?.displayText ?: "Pilih kecamatan",
                items = filter.kecamatan,
                enabled = filter.selectedDati2 != null && filter.kecamatan.isNotEmpty(),
                onSelected = onSelectKecamatan,
            )
            WilayahDropdown(
                label = "Kelurahan",
                selectedText = filter.selectedKelurahan?.displayText ?: "Pilih kelurahan",
                items = filter.kelurahan,
                enabled = filter.selectedKecamatan != null && filter.kelurahan.isNotEmpty(),
                onSelected = onSelectKelurahan,
            )
            WilayahDropdown(
                label = "Blok",
                selectedText = filter.selectedBlok?.displayText ?: "Pilih blok",
                items = filter.blok,
                enabled = filter.selectedKelurahan != null && filter.blok.isNotEmpty(),
                onSelected = onSelectBlok,
            )
            when {
                filter.isLoading -> Text(
                    text = "Memuat referensi wilayah...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                filter.errorMessage != null -> Text(
                    text = filter.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
    }
}

@Composable
private fun WilayahDropdown(
    label: String,
    selectedText: String,
    items: List<WilayahItem>,
    enabled: Boolean,
    onSelected: (WilayahItem?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "$label: $selectedText",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Kosongkan pilihan") },
                onClick = {
                    expanded = false
                    onSelected(null)
                },
            )
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.displayText) },
                    onClick = {
                        expanded = false
                        onSelected(item)
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchStatus(
    uiState: SearchUiState,
    onRetry: () -> Unit,
) {
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
            StateCard(
                title = "Data belum dapat dimuat",
                message = uiState.errorMessage,
                actionText = "Coba Lagi",
                onAction = onRetry,
            )
        }

        uiState.emptyMessage != null -> {
            StateCard(
                title = "Hasil tidak ditemukan",
                message = uiState.emptyMessage,
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
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
