package id.pbbku.mobileportal.feature.home

import androidx.compose.runtime.Composable
import id.pbbku.mobileportal.ui.screen.PlaceholderScreen

@Composable
fun HomeScreen() {
    PlaceholderScreen(
        title = "Beranda",
        items = listOf(
            "Ringkasan status tagihan",
            "Shortcut cari NOP",
            "Riwayat terakhir",
            "Pengingat jatuh tempo",
        ),
    )
}
