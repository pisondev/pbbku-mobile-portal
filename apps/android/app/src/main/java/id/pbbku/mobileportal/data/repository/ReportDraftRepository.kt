package id.pbbku.mobileportal.data.repository

import id.pbbku.mobileportal.data.local.report.ReportDraftDao
import id.pbbku.mobileportal.data.local.report.ReportDraftEntity
import kotlinx.coroutines.flow.Flow

class ReportDraftRepository(
    private val reportDraftDao: ReportDraftDao,
) : RepositoryMarker {
    fun observeAll(): Flow<List<ReportDraftEntity>> {
        return reportDraftDao.observeAll()
    }

    suspend fun getById(id: String): ReportDraftEntity? {
        return reportDraftDao.getById(id)
    }

    suspend fun saveDraft(draft: ReportDraftEntity) {
        reportDraftDao.upsert(draft.copy(updatedAtEpochMillis = System.currentTimeMillis()))
    }

    suspend fun deleteById(id: String) {
        reportDraftDao.deleteById(id)
    }

    suspend fun clearAll() {
        reportDraftDao.clearAll()
    }
}
