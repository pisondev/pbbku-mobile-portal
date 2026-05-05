package id.pbbku.mobileportal.feature.notifications

import androidx.compose.runtime.Composable
import id.pbbku.mobileportal.ui.screen.PlaceholderScreen

@Composable
fun NotificationsScreen() {
    PlaceholderScreen(
        title = "Notifikasi",
        items = listOf(
            "Daftar reminder jatuh tempo",
            "Status reminder lokal",
            "Fallback simulatif bila tanggal jatuh tempo belum tersedia",
        ),
    )
}
