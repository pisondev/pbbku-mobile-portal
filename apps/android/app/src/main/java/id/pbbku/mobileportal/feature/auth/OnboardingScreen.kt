package id.pbbku.mobileportal.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        item {
            AppCard {
                Text(
                    text = "Yang bisa dilakukan",
                    style = MaterialTheme.typography.titleMedium,
                )
                items.forEachIndexed { index, item ->
                    FeatureRow(
                        number = index + 1,
                        text = item,
                    )
                }
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

@Composable
private fun FeatureRow(
    number: Int,
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Text(
                text = number.toString(),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
