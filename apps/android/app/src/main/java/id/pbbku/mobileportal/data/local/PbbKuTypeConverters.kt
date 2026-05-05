package id.pbbku.mobileportal.data.local

import androidx.room.TypeConverter
import id.pbbku.mobileportal.data.local.report.ReportDraftStatus

class PbbKuTypeConverters {
    @TypeConverter
    fun toReportDraftStatus(value: String): ReportDraftStatus {
        return ReportDraftStatus.valueOf(value)
    }

    @TypeConverter
    fun fromReportDraftStatus(value: ReportDraftStatus): String {
        return value.name
    }
}
