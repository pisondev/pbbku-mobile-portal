package id.pbbku.mobileportal.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.pbbku.mobileportal.R
import id.pbbku.mobileportal.core.format.toIndonesianDateTimeText
import id.pbbku.mobileportal.core.security.maskNik
import id.pbbku.mobileportal.data.demo.DemoTaxpayerDirectory
import id.pbbku.mobileportal.data.session.SimulatedSession
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PageHeader

@Composable
fun ProfileScreen(
    session: SimulatedSession?,
) {
    val nik = session?.nik.orEmpty()
    val profile = DemoTaxpayerDirectory.profileForNik(nik)
    val objectCount = DemoTaxpayerDirectory.recordsForNik(nik).size

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
                    text = "Profil Wajib Pajak",
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
                PageHeader(
                    title = profile.name,
                    subtitle = "Informasi pribadi demo dari NIK yang sedang login.",
                    iconRes = R.drawable.ic_nav_settings,
                    titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        item {
            ProfileCard(title = "Identitas") {
                DetailRow("Nama", profile.name)
                RevealableNikRow(profile.nik.ifBlank { "Session tidak tersedia" })
                DetailRow("Alamat", profile.address)
                DetailRow("No HP", profile.phone)
                DetailRow("Email", profile.email)
            }
        }
        item {
            ProfileCard(title = "Akses Objek Pajak") {
                DetailRow("Jenis relasi", profile.relationType)
                DetailRow("Jumlah NOP yang dapat diakses", "$objectCount objek")
                Text(
                    text = "Daftar pada halaman Cari hanya memuat objek pajak yang terhubung dengan NIK ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            ProfileCard(title = "Session") {
                DetailRow("Status", if (session?.isLoggedIn == true) "Aktif" else "Tidak aktif")
                DetailRow(
                    label = "Dibuat",
                    value = session?.createdAtEpochMillis?.toIndonesianDateTimeText()
                        ?: "Data tidak tersedia",
                )
                DetailRow("Token demo", session?.sessionToken ?: "Data tidak tersedia")
            }
        }
    }
}

@Composable
private fun ProfileCard(
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
private fun RevealableNikRow(nik: String) {
    var visible by remember(nik) { mutableStateOf(false) }
    val canReveal = nik.any(Char::isDigit)
    Column(
        modifier = Modifier.clickable(enabled = canReveal) { visible = !visible },
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "NIK",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (visible || !canReveal) nik else nik.maskNik(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (canReveal) {
            Text(
                text = if (visible) "Tekan untuk sembunyikan NIK" else "Tekan untuk lihat NIK lengkap",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
