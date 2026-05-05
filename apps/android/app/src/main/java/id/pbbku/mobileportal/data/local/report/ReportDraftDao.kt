package id.pbbku.mobileportal.data.local.report

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDraftDao {
    @Query("SELECT * FROM report_drafts ORDER BY updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<ReportDraftEntity>>

    @Query("SELECT * FROM report_drafts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ReportDraftEntity?

    @Upsert
    suspend fun upsert(draft: ReportDraftEntity)

    @Query("DELETE FROM report_drafts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM report_drafts")
    suspend fun clearAll()
}
