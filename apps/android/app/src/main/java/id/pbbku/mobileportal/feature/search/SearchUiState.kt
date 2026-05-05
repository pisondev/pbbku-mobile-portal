package id.pbbku.mobileportal.feature.search

import id.pbbku.mobileportal.domain.model.ObjekPajakSummary

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<ObjekPajakSummary> = emptyList(),
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
    val modeLabel: String = "Search",
    val totalRows: Int? = null,
    val canLoadMore: Boolean = false,
)
