package id.pbbku.mobileportal.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.pbbku.mobileportal.data.session.SimulatedSession
import id.pbbku.mobileportal.ui.component.AppCard
import id.pbbku.mobileportal.ui.component.InfoPill
import id.pbbku.mobileportal.ui.component.PageHeader
import id.pbbku.mobileportal.ui.tutorial.TutorialOverlay
import id.pbbku.mobileportal.ui.tutorial.TutorialStep
import id.pbbku.mobileportal.ui.tutorial.rememberTutorialTargetState
import id.pbbku.mobileportal.ui.tutorial.tutorialTarget

@Composable
fun HomeScreen(
    session: SimulatedSession?,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val tutorialTargetState = rememberTutorialTargetState()
    var showTutorial by rememberSaveable { mutableStateOf(true) }
    val tutorialSteps = listOf(
        TutorialStep(
            targetId = "dashboard-guide",
            title = "Ikuti alur demo dari dashboard",
            message = "Checklist ini menyusun flow sesuai SRS dan kontrak: cari objek, baca detail, cek tagihan, lalu gunakan fitur pendukung.",
        ),
        TutorialStep(
            targetId = "home-search-action",
            title = "Mulai dengan pencarian objek pajak",
            message = "Tekan tombol ini untuk mencari NOP atau nama wajib pajak. Dari hasil pencarian kamu bisa masuk ke detail objek.",
            actionLabel = "Buka Cari",
        ),
        TutorialStep(
            targetId = "home-notification-action",
            title = "Pantau pengingat jatuh tempo",
            message = "Halaman notifikasi menampilkan reminder lokal 30, 7, dan 1 hari sebelum jatuh tempo jika datanya tersedia.",
            actionLabel = "Lihat Notifikasi",
        ),
    )

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
                        text = "Session simulatif aktif",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                    PageHeader(
                        title = "Halo, ${session?.displayName ?: "Wajib Pajak Demo"}",
                        subtitle = "NIK: ${session?.maskedNik ?: "Tidak tersedia"}",
                    )
                    Text(
                        text = "Portal wajib pajak untuk melihat objek PBB, SPPT, tunggakan, reminder, dan draft laporan perubahan bangunan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Informasi penting",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Data pembayaran dan laporan pada aplikasi ini bersifat demo read-only. Pembayaran resmi tetap dilakukan melalui kanal pemerintah daerah atau mitra yang ditunjuk.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item {
                DashboardGuideCard(
                    modifier = Modifier.tutorialTarget(tutorialTargetState, "dashboard-guide"),
                    onOpenSearch = onOpenSearch,
                    onOpenNotifications = onOpenNotifications,
                )
            }
            item {
                ActionCard(
                    title = "Mulai dari NOP atau nama wajib pajak",
                    description = "Cari objek pajak, lalu buka detail, SPPT, tunggakan, bangunan, atau laporan perubahan.",
                    label = "Alur utama",
                ) {
                    Button(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .tutorialTarget(tutorialTargetState, "home-search-action"),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .tutorialTarget(tutorialTargetState, "home-notification-action"),
                    ) {
                        Text("Lihat Notifikasi")
                    }
                }
            }
            item {
                TermsCard()
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
private fun DashboardGuideCard(
    modifier: Modifier = Modifier,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                InfoPill(text = "Panduan pemula")
                Text(
                    text = "Langkah utama demo PBB-Ku",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Urutan ini mengikuti kebutuhan SRS dan kontrak MVP agar pengguna baru tidak tersesat saat mencoba aplikasi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        GuideStepRow(
            number = 1,
            title = "Cari NOP atau nama wajib pajak",
            description = "Masuk ke halaman pencarian dan pilih hasil yang relevan.",
            onClick = onOpenSearch,
        )
        GuideStepRow(
            number = 2,
            title = "Baca profil objek dan subjek pajak",
            description = "Dari detail objek, lanjut ke bangunan, histori SPPT, tunggakan, atau laporan perubahan.",
            onClick = onOpenSearch,
        )
        GuideStepRow(
            number = 3,
            title = "Cek tagihan dan informasi pembayaran",
            description = "Histori SPPT dan tunggakan menunjukkan status, nominal, serta jatuh tempo tanpa transaksi nyata.",
            onClick = onOpenSearch,
        )
        GuideStepRow(
            number = 4,
            title = "Buat draft laporan perubahan bangunan",
            description = "Laporan hanya prototipe lokal dan tidak mengubah data resmi SIMPBB.",
            onClick = onOpenSearch,
        )
        GuideStepRow(
            number = 5,
            title = "Pantau reminder dan pengaturan",
            description = "Notifikasi lokal membantu mengingat jatuh tempo jika tanggal tersedia.",
            onClick = onOpenNotifications,
        )
    }
}

@Composable
private fun GuideStepRow(
    number: Int,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Text(
                text = number.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = ">",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
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
