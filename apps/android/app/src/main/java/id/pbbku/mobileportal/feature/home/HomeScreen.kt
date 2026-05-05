package id.pbbku.mobileportal.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.pbbku.mobileportal.data.session.SimulatedSession
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PageHeader

@Composable
fun HomeScreen(
    session: SimulatedSession?,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                InfoPill(
                    text = "Session simulatif aktif",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
                PageHeader(
                    title = "Beranda",
                    subtitle = "NIK: ${session?.maskedNik ?: "Tidak tersedia"}",
                )
                Text(
                    text = "Mulai dari pencarian objek pajak untuk melihat detail, tagihan, bangunan, dan laporan perubahan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        item {
            ActionCard(
                title = "Mulai dari NOP atau nama wajib pajak",
                description = "Cari objek pajak, lalu buka detail, SPPT, tunggakan, bangunan, atau laporan perubahan.",
                label = "Alur utama",
            ) {
                Button(
                    onClick = onOpenSearch,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cari Objek Pajak")
                }
            }
        }
        item {
            ActionCard(
                title = "Pengingat jatuh tempo",
                description = "Reminder bersifat lokal di perangkat dan tidak berasal dari server Bapenda.",
                label = "Notifikasi lokal",
            ) {
                OutlinedButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Lihat Notifikasi")
                }
            }
        }
        item {
            TermsCard()
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    label: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                InfoPill(text = label)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        content()
    }
}

@Composable
private fun TermsCard() {
    ActionCard(
        title = "Istilah PBB",
        description = "Ringkasan istilah yang sering muncul di aplikasi.",
        label = "Bantuan cepat",
    ) {
        TermText("NOP", "Nomor Objek Pajak untuk mengenali tanah atau bangunan.")
        TermText("NJOP", "Nilai Jual Objek Pajak yang menjadi dasar perhitungan PBB.")
        TermText("SPPT", "Surat Pemberitahuan Pajak Terutang per tahun pajak.")
        TermText("SSPD", "Bukti setoran pajak daerah. Pada MVP hanya ditampilkan sebagai prototipe bila tersedia.")
        TermText("Tunggakan", "Tagihan PBB yang belum lunas atau melewati jatuh tempo.")
    }
}

@Composable
private fun TermText(
    term: String,
    description: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = term,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
