package id.pbbku.mobileportal.feature.report

import id.pbbku.mobileportal.data.local.report.ReportDraftStatus
import id.pbbku.mobileportal.domain.model.BuildingDetail
import id.pbbku.mobileportal.domain.model.BuildingSummary
import id.pbbku.mobileportal.domain.model.Nop

data class ReportDraftUiState(
    val nop: Nop? = null,
    val noBng: String = "",
    val availableBuildings: List<BuildingSummary> = emptyList(),
    val changeType: String = "Perubahan luas bangunan",
    val oldBuildingAreaText: String = "",
    val newBuildingAreaText: String = "",
    val oldFloorCountText: String = "",
    val newFloorCountText: String = "",
    val description: String = "",
    val status: ReportDraftStatus = ReportDraftStatus.DRAFT,
    val isLoadingBuilding: Boolean = false,
    val oldBuildingDetail: BuildingDetail? = null,
    val isAccessAllowed: Boolean = true,
    val buildingMessage: String? = null,
    val validation: ReportDraftValidationResult = ReportDraftValidationResult(),
    val saveMessage: String? = null,
    val showSummary: Boolean = false,
    val isDeleting: Boolean = false,
) {
    val selectedBuilding: BuildingSummary? = availableBuildings.firstOrNull { it.noBng == noBng }
    val showsAreaFields: Boolean = changeType == "Perubahan luas bangunan"
    val showsFloorFields: Boolean = changeType == "Perubahan jumlah lantai"
    val showsDescriptionOnly: Boolean = changeType == "Perubahan data bangunan"
}
