package id.pbbku.mobileportal.feature.building

import id.pbbku.mobileportal.domain.model.BuildingDetail
import id.pbbku.mobileportal.domain.model.BuildingFacility
import id.pbbku.mobileportal.domain.model.Nop

data class BuildingDetailUiState(
    val nop: Nop? = null,
    val noBng: String = "",
    val isLoading: Boolean = false,
    val detail: BuildingDetail? = null,
    val facilities: List<BuildingFacility> = emptyList(),
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
    val facilitiesMessage: String? = null,
)
