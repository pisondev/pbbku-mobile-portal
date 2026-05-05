package id.pbbku.mobileportal.feature.home

import androidx.compose.runtime.Composable
import id.pbbku.mobileportal.data.session.SimulatedSession
import id.pbbku.mobileportal.ui.screen.PlaceholderScreen

@Composable
fun HomeScreen(
    session: SimulatedSession?,
) {
    PlaceholderScreen(
        title = "Beranda",
        items = listOf(
            "Session aktif: ${session?.maskedNik ?: "NIK tersamarkan"}",
            "Ringkasan status tagihan",
            "Shortcut cari NOP",
            "Riwayat terakhir",
            "Pengingat jatuh tempo",
        ),
    )
}
