package id.pbbku.mobileportal.feature.search

import androidx.compose.runtime.Composable
import id.pbbku.mobileportal.ui.screen.PlaceholderScreen

@Composable
fun SearchScreen() {
    PlaceholderScreen(
        title = "Cari Objek Pajak",
        items = listOf(
            "Pencarian NOP atau nama wajib pajak",
            "Daftar hasil pencarian",
            "Filter wilayah",
            "Aksi buka detail objek pajak",
        ),
    )
}
