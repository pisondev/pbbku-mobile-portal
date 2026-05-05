package id.pbbku.mobileportal.feature.settings

import androidx.compose.runtime.Composable
import id.pbbku.mobileportal.ui.screen.PlaceholderScreen

@Composable
fun SettingsScreen() {
    PlaceholderScreen(
        title = "Pengaturan",
        items = listOf(
            "Preferensi notifikasi",
            "Hapus cache",
            "Hapus draft laporan",
            "Logout session simulatif",
        ),
    )
}
