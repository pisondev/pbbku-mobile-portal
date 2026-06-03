package id.pbbku.mobileportal.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.R
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.data.session.SimulatedSession
import id.pbbku.mobileportal.domain.model.ObjekPajakSummary
import id.pbbku.mobileportal.domain.model.WilayahItem
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.LoadingSkeletonCard
import id.pbbku.mobileportal.ui.component.PageHelpButton
import id.pbbku.mobileportal.ui.component.PrimaryGradientCard
import id.pbbku.mobileportal.ui.component.SectionTitleWithIcon
import id.pbbku.mobileportal.ui.component.StateCard
import id.pbbku.mobileportal.ui.tutorial.TutorialOverlay
import id.pbbku.mobileportal.ui.tutorial.TutorialStep
import id.pbbku.mobileportal.ui.tutorial.rememberTutorialVisibilityState
import id.pbbku.mobileportal.ui.tutorial.rememberTutorialTargetState
import id.pbbku.mobileportal.ui.tutorial.tutorialTarget

@Composable
fun SearchScreen(
    onOpenDetail: (String) -> Unit,
    session: SimulatedSession? = null,
    helpRequestId: Int,
    onRequestHelp: () -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val tutorialTargetState = rememberTutorialTargetState()
    val tutorialVisibility = rememberTutorialVisibilityState(
        pageKey = "search",
        helpRequestId = helpRequestId,
    )
    val tutorialSteps = listOf(
        TutorialStep(
            targetId = "search-input",
            title = "Ketik NOP atau nama wajib pajak",
            message = "Masukkan minimal 3 karakter agar aplikasi dapat menampilkan objek pajak yang sesuai.",
        ),
        TutorialStep(
            targetId = "demo-list",
            title = "Lihat daftar objek pajak",
            message = "Tombol ini memuat daftar objek pajak yang tersedia agar kamu bisa memilih data yang ingin diperiksa.",
            actionLabel = "Muat Daftar",
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
                PrimaryGradientCard {
                    InfoPill(
                        text = "Pencarian SIMPBB",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                    SectionTitleWithIcon(
                        title = "Cari Objek",
                        iconRes = R.drawable.ic_nav_search,
                        contentColor = Color.White,
                    )
                    Text(
                        text = "Masukkan NOP atau nama wajib pajak. Hasil detail tetap dibaca dari data resmi SIMPBB OP API.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFFE0F2FE).copy(alpha = 0.94f),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Informasi Penting",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF075985),
                            )
                            Text(
                                text = "Gunakan minimal 3 karakter untuk pencarian cepat. Filter wilayah bisa dibuka untuk mempersempit daftar objek pajak.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF0F172A),
                            )
                        }
                    }
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .tutorialTarget(tutorialTargetState, "search-input"),
                        label = { Text("NOP atau nama wajib pajak") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_nav_search),
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        supportingText = {
                            Text(
                                text = "Pencarian otomatis setelah minimal 3 karakter.",
                                color = Color.White.copy(alpha = 0.86f),
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
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
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.48f),
                            ),
                        ) {
                            Text("Daftar Objek")
                        }
                        OutlinedButton(
                            onClick = viewModel::retry,
                            enabled = uiState.errorMessage != null,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.48f),
                            ),
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
            visible = tutorialVisibility.visible,
            steps = tutorialSteps,
            targetState = tutorialTargetState,
            modifier = Modifier.align(Alignment.BottomCenter),
            onDismiss = tutorialVisibility.dismiss,
        )
        PageHelpButton(
            onClick = onRequestHelp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
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
    var expanded by rememberSaveable { mutableStateOf(false) }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text("Filter Wilayah", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = if (filter.hasAnySelection) "Filter aktif" else "Persempit hasil berdasarkan wilayah",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Sembunyikan" else "Tampilkan")
            }
        }
        if (expanded) {
            FilterRow {
                WilayahDropdown(
                    label = "Provinsi",
                    selectedText = filter.selectedPropinsi?.displayText ?: "Pilih provinsi",
                    items = filter.propinsi,
                    enabled = filter.propinsi.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    onSelected = onSelectPropinsi,
                )
                WilayahDropdown(
                    label = "Kab/Kota",
                    selectedText = filter.selectedDati2?.displayText ?: "Pilih kab/kota",
                    items = filter.dati2,
                    enabled = filter.selectedPropinsi != null && filter.dati2.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    onSelected = onSelectDati2,
                )
            }
            FilterRow {
                WilayahDropdown(
                    label = "Kecamatan",
                    selectedText = filter.selectedKecamatan?.displayText ?: "Pilih kecamatan",
                    items = filter.kecamatan,
                    enabled = filter.selectedDati2 != null && filter.kecamatan.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    onSelected = onSelectKecamatan,
                )
                WilayahDropdown(
                    label = "Kelurahan",
                    selectedText = filter.selectedKelurahan?.displayText ?: "Pilih kelurahan",
                    items = filter.kelurahan,
                    enabled = filter.selectedKecamatan != null && filter.kelurahan.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    onSelected = onSelectKelurahan,
                )
            }
            FilterRow {
                WilayahDropdown(
                    label = "Blok",
                    selectedText = filter.selectedBlok?.displayText ?: "Pilih blok",
                    items = filter.blok,
                    enabled = filter.selectedKelurahan != null && filter.blok.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    onSelected = onSelectBlok,
                )
                Box(modifier = Modifier.weight(1f))
            }
            OutlinedButton(
                onClick = onClear,
                enabled = filter.hasAnySelection,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reset Filter")
            }
            when {
                filter.isLoading -> FilterLoadingPlaceholder()
                filter.errorMessage != null -> Text(
                    text = filter.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun FilterLoadingPlaceholder() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(2) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == 0) 0.72f else 0.48f)
                    .height(14.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    ),
            )
        }
    }
}

@Composable
private fun FilterRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun WilayahDropdown(
    label: String,
    selectedText: String,
    items: List<WilayahItem>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSelected: (WilayahItem?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = selectedText,
                    maxLines = 1,
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
}

@Composable
private fun SearchStatus(
    uiState: SearchUiState,
    onRetry: () -> Unit,
) {
    when {
        uiState.isLoading -> {
            LoadingSkeletonCard()
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
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    text = (result.namaWajibPajak ?: "W").first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = result.namaWajibPajak ?: "Nama WP tidak tersedia",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    InfoPill(
                        text = "Detail",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = result.nop.asGroupedText(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = result.alamatObjekPajak ?: "Alamat objek tidak tersedia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                result.njopBumi?.let {
                    Text(
                        text = "NJOP bumi ${it.toRupiahText()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}
