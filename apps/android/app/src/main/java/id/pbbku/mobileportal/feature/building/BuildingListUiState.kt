package id.pbbku.mobileportal.feature.building

import id.pbbku.mobileportal.domain.model.BuildingSummary
import id.pbbku.mobileportal.domain.model.Nop

data class BuildingListUiState(
    val nop: Nop? = null,
    val isLoading: Boolean = false,
    val buildings: List<BuildingSummary> = emptyList(),
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
)
