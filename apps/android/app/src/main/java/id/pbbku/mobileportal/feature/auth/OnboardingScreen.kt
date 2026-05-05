package id.pbbku.mobileportal.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
) {
    val items = listOf(
        "Cari objek pajak berdasarkan NOP atau nama wajib pajak.",
        "Lihat histori SPPT, tagihan, dan tunggakan.",
        "Simpan pengingat lokal untuk jatuh tempo pembayaran.",
        "Buat draft laporan perubahan bangunan sebagai prototipe.",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            AppCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                InfoPill(
                    text = "Portal wajib pajak PBB-P2",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "PBB-Ku",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Akses objek pajak, SPPT, tunggakan, pengingat, dan draft laporan perubahan bangunan dari satu aplikasi Android.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        items(items.withIndex().toList()) { indexedItem ->
            AppCard(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                InfoPill(text = "Fitur ${indexedItem.index + 1}")
                Text(
                    text = indexedItem.value,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        item {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text("Masuk")
            }
        }
    }
}
