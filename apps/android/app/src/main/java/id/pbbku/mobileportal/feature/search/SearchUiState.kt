package id.pbbku.mobileportal.feature.search

import id.pbbku.mobileportal.domain.model.ObjekPajakSummary
import id.pbbku.mobileportal.domain.model.WilayahItem

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<ObjekPajakSummary> = emptyList(),
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
    val modeLabel: String = "Pencarian",
    val totalRows: Int? = null,
    val canLoadMore: Boolean = false,
    val wilayahFilter: WilayahFilterUiState = WilayahFilterUiState(),
)

data class WilayahFilterUiState(
    val propinsi: List<WilayahItem> = emptyList(),
    val dati2: List<WilayahItem> = emptyList(),
    val kecamatan: List<WilayahItem> = emptyList(),
    val kelurahan: List<WilayahItem> = emptyList(),
    val blok: List<WilayahItem> = emptyList(),
    val selectedPropinsi: WilayahItem? = null,
    val selectedDati2: WilayahItem? = null,
    val selectedKecamatan: WilayahItem? = null,
    val selectedKelurahan: WilayahItem? = null,
    val selectedBlok: WilayahItem? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasAnySelection: Boolean =
        selectedPropinsi != null ||
            selectedDati2 != null ||
            selectedKecamatan != null ||
            selectedKelurahan != null ||
            selectedBlok != null
}
