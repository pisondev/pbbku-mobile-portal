package id.pbbku.mobileportal.data.repository

import id.pbbku.mobileportal.data.local.cache.CacheEntryDao
import id.pbbku.mobileportal.data.local.cache.CacheEntryEntity
import kotlinx.coroutines.flow.Flow

class LocalCacheRepository(
    private val cacheEntryDao: CacheEntryDao,
) : RepositoryMarker {
    fun observeByKey(cacheKey: String): Flow<CacheEntryEntity?> {
        return cacheEntryDao.observeByKey(cacheKey)
    }

    suspend fun getByKey(cacheKey: String): CacheEntryEntity? {
        return cacheEntryDao.getByKey(cacheKey)
    }

    suspend fun saveReadOnlyCache(
        cacheKey: String,
        payloadJson: String,
        sourceLabel: String,
        updatedAtEpochMillis: Long = System.currentTimeMillis(),
    ) {
        cacheEntryDao.upsert(
            CacheEntryEntity(
                cacheKey = cacheKey,
                payloadJson = payloadJson,
                sourceLabel = sourceLabel,
                updatedAtEpochMillis = updatedAtEpochMillis,
            ),
        )
    }

    suspend fun clearAll() {
        cacheEntryDao.clearAll()
    }
}
