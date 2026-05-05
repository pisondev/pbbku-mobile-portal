package id.pbbku.mobileportal.feature.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun PaymentInfoScreen(
    nopDisplay: String,
    taxYear: String,
    onBack: () -> Unit,
    viewModel: PaymentInfoViewModel = viewModel(),
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
                nopText = uiState.nop?.asGroupedText() ?: nopDisplay,
                taxYear = uiState.taxYear?.toString() ?: taxYear,
                onBack = onBack,
            )
        }
        item {
            StatusBlock(
                uiState = uiState,
                onRetry = viewModel::retry,
            )
        }
        item {
            PaymentStatusCard(uiState)
        }
        item {
            PaymentInstructionCard()
        }
        item {
            PaymentChannelCard()
        }
        if (uiState.detail?.status == PaymentStatus.PAID) {
            item {
                SspdPrototypeCard(uiState)
            }
        }
    }
}

@Composable
private fun HeaderBlock(
    nopText: String,
    taxYear: String,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack) {
            Text("Kembali")
        }
        Text(
            text = "Informasi Pembayaran",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "NOP: $nopText",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Tahun pajak: $taxYear",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusBlock(
    uiState: PaymentInfoUiState,
    onRetry: () -> Unit,
) {
    when {
        uiState.isLoading -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CircularProgressIndicator()
            Text("Memuat status tagihan...")
        }
        uiState.errorMessage != null -> ErrorBlock(uiState.errorMessage, onRetry)
        uiState.emptyMessage != null -> Text(
            text = uiState.emptyMessage,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PaymentStatusCard(uiState: PaymentInfoUiState) {
    val detail = uiState.detail
    val summary = uiState.summary
    val status = detail?.status ?: summary?.status ?: PaymentStatus.UNKNOWN
    val amount = detail?.amount ?: summary?.amount
    val dueDate = detail?.dueDate ?: summary?.dueDate
    val fine = detail?.fine ?: summary?.fine

    DetailCard(title = "Status Tagihan") {
        DetailRow("Status", status.toPaymentStatusText(), status.statusColor())
        DetailRow("Nominal tagihan", amount?.toRupiahText())
        DetailRow("Jatuh tempo", dueDate?.toIndonesianDateText())
        DetailRow("Denda", fine?.toRupiahText())
        DetailRow("Tanggal pembayaran", detail?.paymentDate?.toIndonesianDateText())
        if (!uiState.hasBillData) {
            Text(
                text = "Status ini belum dapat diverifikasi dari API untuk tagihan yang dipilih.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PaymentInstructionCard() {
    DetailCard(title = "Arahan Pembayaran") {
        Text(
            text = "Aplikasi ini tidak memproses pembayaran dan tidak membuat transaksi.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        BulletText("Gunakan NOP dan tahun pajak yang tertera saat melakukan pembayaran melalui kanal resmi pemerintah daerah atau bank/mitra pembayaran yang ditunjuk.")
        BulletText("Pastikan nominal dan status tagihan sesuai dengan data pada kanal resmi sebelum membayar.")
        BulletText("Simpan bukti pembayaran dari kanal resmi. Bukti di aplikasi ini hanya prototipe bila ditampilkan.")
    }
}

@Composable
private fun PaymentChannelCard() {
    DetailCard(title = "Kanal Pembayaran") {
        Text(
            text = "Konten kanal berikut bersifat informasi umum/demo karena tidak ada endpoint pembayaran resmi pada MVP.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        BulletText("Loket atau kanal resmi Bapenda/pemerintah daerah.")
        BulletText("Bank daerah atau mitra pembayaran yang diumumkan resmi oleh Bapenda.")
        BulletText("ATM, mobile banking, atau marketplace hanya jika kanal tersebut terdaftar resmi untuk PBB-P2 daerah terkait.")
    }
}

@Composable
private fun SspdPrototypeCard(uiState: PaymentInfoUiState) {
    val detail = uiState.detail ?: return
    DetailCard(title = "Bukti/SSPD Prototipe") {
        Text(
            text = "PROTOTIPE - bukan bukti pembayaran resmi.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleSmall,
        )
        DetailRow("NOP", detail.nop.asGroupedText())
        DetailRow("Tahun pajak", detail.taxYear.toString())
        DetailRow("Nominal dibayar", detail.amount?.toRupiahText())
        DetailRow("Tanggal pembayaran", detail.paymentDate?.toIndonesianDateText())
        DetailRow("Status", detail.status.toPaymentStatusText(), detail.status.statusColor())
        Text(
            text = "Tidak ada QR pembayaran, nomor virtual account, atau bukti resmi yang dibuat oleh aplikasi.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
    color: Color = MaterialTheme.colorScheme.onSurface,
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
            color = color,
        )
    }
}

@Composable
private fun BulletText(text: String) {
    Text(
        text = "- $text",
        style = MaterialTheme.typography.bodyMedium,
    )
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
private fun PaymentStatus.statusColor(): Color {
    return when (this) {
        PaymentStatus.PAID -> Color(0xFF047857)
        PaymentStatus.UNPAID -> MaterialTheme.colorScheme.tertiary
        PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.error
        PaymentStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
