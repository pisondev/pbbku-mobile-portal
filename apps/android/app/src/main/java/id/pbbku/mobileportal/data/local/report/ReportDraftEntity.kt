package id.pbbku.mobileportal.data.local.report

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report_drafts")
data class ReportDraftEntity(
    @PrimaryKey val id: String,
    val nopDisplay: String,
    val noBng: String?,
    val changeType: String,
    val oldBuildingArea: Double?,
    val newBuildingArea: Double?,
    val oldFloorCount: Int?,
    val newFloorCount: Int?,
    val description: String,
    val status: ReportDraftStatus,
    val updatedAtEpochMillis: Long,
)
