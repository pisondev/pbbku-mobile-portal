package id.pbbku.mobileportal.feature.sppt

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.pbbku.mobileportal.core.format.toIndonesianDateText
import id.pbbku.mobileportal.core.format.toPaymentStatusText
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.PaymentStatus
import id.pbbku.mobileportal.domain.model.TaxBillDetail
import id.pbbku.mobileportal.domain.model.TaxBillSummary

@Composable
fun SpptHistoryScreen(
    nopDisplay: String,
    onBack: () -> Unit,
    onOpenDetail: (String, Int) -> Unit,
    onOpenPayment: (String, Int) -> Unit,
    viewModel: SpptHistoryViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(nopDisplay) {
        viewModel.load(nopDisplay)
    }
    TaxBillListScreenContent(
        title = "Histori SPPT",
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onOpenDetail = onOpenDetail,
        onOpenPayment = onOpenPayment,
        showTotal = false,
    )
}

@Composable
fun TunggakanScreen(
    nopDisplay: String,
    onBack: () -> Unit,
    onOpenDetail: (String, Int) -> Unit,
    onOpenPayment: (String, Int) -> Unit,
    viewModel: TunggakanViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(nopDisplay) {
        viewModel.load(nopDisplay)
    }
    TaxBillListScreenContent(
        title = "Tunggakan",
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onOpenDetail = onOpenDetail,
        onOpenPayment = onOpenPayment,
        showTotal = true,
    )
}

@Composable
fun TaxBillDetailScreen(
    nopDisplay: String,
    taxYear: String,
    onBack: () -> Unit,
    onOpenPayment: (String, Int) -> Unit,
    viewModel: TaxBillDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(nopDisplay, taxYear) {
        viewModel.load(nopDisplay, taxYear)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeaderBlock(
                title = "Detail Tagihan",
                nopText = uiState.nop?.asGroupedText(),
                onBack = onBack,
            )
        }
        item {
            DetailStatus(
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                emptyMessage = uiState.emptyMessage,
                onRetry = viewModel::retry,
            )
        }
        uiState.detail?.let { detail ->
            item {
                DetailBillCard(
                    detail = detail,
                    onOpenPayment = onOpenPayment,
                )
            }
        }
    }
}

@Composable
private fun TaxBillListScreenContent(
    title: String,
    uiState: TaxBillListUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenDetail: (String, Int) -> Unit,
    onOpenPayment: (String, Int) -> Unit,
    showTotal: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeaderBlock(
                title = title,
                nopText = uiState.nop?.asGroupedText(),
                onBack = onBack,
            )
        }
        if (showTotal && uiState.totalActiveAmount != null) {
            item {
                DetailCard(title = "Total Tunggakan Aktif") {
                    Text(
                        text = uiState.totalActiveAmount.toRupiahText(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        item {
            ListStatus(
                uiState = uiState,
                onRetry = onRetry,
            )
        }
        items(
            items = uiState.bills,
            key = { "${it.nop.asDisplayText()}-${it.taxYear}" },
        ) { bill ->
            BillSummaryCard(
                bill = bill,
                onOpenDetail = { onOpenDetail(bill.nop.asDisplayText(), bill.taxYear) },
                onOpenPayment = { onOpenPayment(bill.nop.asDisplayText(), bill.taxYear) },
            )
        }
    }
}

@Composable
private fun HeaderBlock(
    title: String,
    nopText: String?,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack) {
            Text("Kembali")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        nopText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ListStatus(
    uiState: TaxBillListUiState,
    onRetry: () -> Unit,
) {
    when {
        uiState.isLoading -> LoadingRow("Memuat tagihan...")
        uiState.errorMessage != null -> ErrorBlock(uiState.errorMessage, onRetry)
        uiState.emptyMessage != null -> EmptyText(uiState.emptyMessage)
        uiState.bills.isNotEmpty() -> Text(
            text = "${uiState.bills.size} data tagihan ditemukan.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DetailStatus(
    isLoading: Boolean,
    errorMessage: String?,
    emptyMessage: String?,
    onRetry: () -> Unit,
) {
    when {
        isLoading -> LoadingRow("Memuat detail tagihan...")
        errorMessage != null -> ErrorBlock(errorMessage, onRetry)
        emptyMessage != null -> EmptyText(emptyMessage)
    }
}

@Composable
private fun BillSummaryCard(
    bill: TaxBillSummary,
    onOpenDetail: () -> Unit,
    onOpenPayment: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Tahun ${bill.taxYear}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = bill.status.toPaymentStatusText(),
                    style = MaterialTheme.typography.labelLarge,
                    color = bill.status.statusColor(),
                )
            }
            Text(
                text = bill.amount?.toRupiahText() ?: "Nominal tidak tersedia",
                style = MaterialTheme.typography.headlineSmall,
            )
            DetailRow("Jatuh tempo", bill.dueDate?.toIndonesianDateText())
            DetailRow("Denda", bill.fine?.toRupiahText())
            if (bill.isPayable) {
                OutlinedButton(
                    onClick = onOpenPayment,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Lihat Cara Bayar")
                }
            }
        }
    }
}

@Composable
private fun DetailBillCard(
    detail: TaxBillDetail,
    onOpenPayment: (String, Int) -> Unit,
) {
    DetailCard(title = "Tagihan Tahun ${detail.taxYear}") {
        DetailRow("Status", detail.status.toPaymentStatusText())
        DetailRow("Nominal tagihan", detail.amount?.toRupiahText())
        DetailRow("Jatuh tempo", detail.dueDate?.toIndonesianDateText())
        DetailRow("Denda", detail.fine?.toRupiahText())
        DetailRow("Tanggal pembayaran", detail.paymentDate?.toIndonesianDateText())
        DetailRow("NJOP tanah", detail.njopBumi?.toRupiahText())
        DetailRow("NJOP bangunan", detail.njopBangunan?.toRupiahText())
        DetailRow("NJOP total", detail.njopTotal?.toRupiahText())
        DetailRow("NJOPTKP", detail.njoptkp?.toRupiahText())
        DetailRow("Tarif", detail.tarif?.let { "$it%" })
        DetailRow("PBB terutang", detail.pbbTerutang?.toRupiahText())
        if (detail.isPayable) {
            Button(
                onClick = { onOpenPayment(detail.nop.asDisplayText(), detail.taxYear) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Lihat Cara Bayar")
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            content()
        }
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

@Composable
private fun LoadingRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CircularProgressIndicator()
        Text(text)
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyText(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun PaymentStatus.statusColor(): Color {
    return when (this) {
        PaymentStatus.PAID -> Color(0xFF047857)
        PaymentStatus.UNPAID -> MaterialTheme.colorScheme.tertiary
        PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.error
        PaymentStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
