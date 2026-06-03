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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import id.pbbku.mobileportal.R
import id.pbbku.mobileportal.core.format.toIndonesianDateTimeText
import id.pbbku.mobileportal.core.format.toIndonesianDateText
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.domain.model.PaymentAttempt
import id.pbbku.mobileportal.domain.model.PaymentAttemptStatus
import id.pbbku.mobileportal.domain.model.PaymentStatus
import id.pbbku.mobileportal.domain.model.SspdReceipt
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.LoadingSkeletonCard
import id.pbbku.mobileportal.ui.component.PageHeader
import id.pbbku.mobileportal.ui.component.PaymentStatusLabel
import id.pbbku.mobileportal.ui.component.statusColor

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
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
        uiState.paymentFlow?.let { flow ->
            if (flow.paymentAttempt != null && flow.taxBill.isPayable) {
                item {
                    PaymentAttemptCard(
                        uiState = uiState,
                        payment = flow.paymentAttempt,
                        taxpayerName = flow.taxpayerName,
                        maskedNik = flow.maskedNik,
                        objectAddress = flow.objectAddress,
                        onSimulateSuccess = viewModel::simulatePaymentSuccess,
                    )
                }
            }
            flow.receipt?.let { receipt ->
                item {
                    SspdReceiptCard(receipt = receipt)
                }
            }
        }
        item {
            PaymentChannelCard()
        }
    }
}

@Composable
private fun HeaderBlock(
    nopText: String,
    taxYear: String,
    onBack: () -> Unit,
) {
    AppCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        InfoPill(
            text = "Payment demo",
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        )
        PageHeader(
            title = "Pembayaran PBB Demo",
            subtitle = "Simulasi payment attempt tanpa payment gateway asli.",
            iconRes = R.drawable.shortcut_tunggakan,
            titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
        uiState.isLoading -> LoadingSkeletonCard()
        uiState.errorMessage != null -> ErrorBlock(uiState.errorMessage, onRetry)
        uiState.emptyMessage != null -> Text(
            text = uiState.emptyMessage,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        uiState.actionMessage != null -> Text(
            text = uiState.actionMessage,
            color = MaterialTheme.colorScheme.primary,
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
        PaymentStatusLabel(status = status)
        DetailRow("Nominal tagihan", amount?.toRupiahText(), status.statusColor())
        DetailRow("Jatuh tempo", dueDate?.toIndonesianDateText(), status.statusColor())
        DetailRow("Denda", fine?.toRupiahText())
        DetailRow("Tanggal pembayaran", detail?.paymentDate?.toIndonesianDateText())
        if (detail?.isOverdue == true) {
            InfoPill(
                text = "Tunggakan",
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error,
            )
        }
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
private fun PaymentAttemptCard(
    uiState: PaymentInfoUiState,
    payment: PaymentAttempt,
    taxpayerName: String?,
    maskedNik: String,
    objectAddress: String?,
    onSimulateSuccess: () -> Unit,
) {
    DetailCard(title = "Payment Attempt") {
        DetailRow("Nama wajib pajak", taxpayerName)
        DetailRow("NIK", maskedNik)
        DetailRow("NOP", payment.nop.asGroupedText())
        DetailRow("Tahun pajak", payment.taxYear.toString())
        DetailRow("Alamat objek", objectAddress)
        DetailRow("Pokok PBB", payment.principalAmount.toRupiahText())
        DetailRow("Denda", payment.penaltyAmount.toRupiahText())
        DetailRow("Total bayar", payment.totalAmount.toRupiahText(), MaterialTheme.colorScheme.primary)
        DetailRow("Metode", payment.method)
        DetailRow("Kode pembayaran", payment.paymentCode)
        DetailRow("Status payment", payment.status.label)
        if (payment.status == PaymentAttemptStatus.PENDING) {
            Text(
                text = "Tombol di bawah mensimulasikan callback sukses dari payment gateway.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Button(
                onClick = onSimulateSuccess,
                enabled = !uiState.isSimulatingPayment,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSimulatingPayment) "Memproses..." else "Simulate Payment Success")
            }
        }
    }
}

@Composable
private fun SspdReceiptCard(receipt: SspdReceipt) {
    DetailCard(title = "Bukti Pembayaran PBB / SSPD") {
        InfoPill(
            text = "LUNAS",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        )
        DetailRow("Nomor SSPD", receipt.sspdNumber)
        DetailRow("Tanggal terbit", receipt.issuedAtEpochMillis.toIndonesianDateTimeText())
        DetailRow("Nama wajib pajak", receipt.namaWajibPajak)
        DetailRow("NIK", receipt.nik.takeLast(4).padStart(receipt.nik.length, '*'))
        DetailRow("NOP", receipt.nop.asGroupedText())
        DetailRow("Tahun pajak", receipt.taxYear.toString())
        DetailRow("Pokok pajak", receipt.principalAmount.toRupiahText())
        DetailRow("Denda", receipt.penaltyAmount.toRupiahText())
        DetailRow("Total dibayar", receipt.totalPaid.toRupiahText(), MaterialTheme.colorScheme.primary)
        DetailRow("Metode pembayaran", receipt.method)
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
        BulletText("Simpan bukti pembayaran dari kanal resmi sebagai arsip pribadi.")
    }
}

@Composable
private fun PaymentChannelCard() {
    DetailCard(title = "Kanal Pembayaran") {
        Text(
            text = "Kanal berikut adalah dummy untuk demo. Tidak ada pembayaran asli yang diproses.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        BulletText("QRIS Demo untuk tagihan berjalan.")
        BulletText("Virtual Account Demo untuk tunggakan.")
        BulletText("Bank Transfer Demo untuk riwayat pembayaran lunas.")
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard {
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
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
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
            Text("Coba Lagi")
        }
    }
}
