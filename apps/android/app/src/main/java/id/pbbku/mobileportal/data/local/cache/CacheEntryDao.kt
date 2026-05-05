package id.pbbku.mobileportal.data.local.cache

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheEntryDao {
    @Query("SELECT * FROM cache_entries WHERE cacheKey = :cacheKey LIMIT 1")
    fun observeByKey(cacheKey: String): Flow<CacheEntryEntity?>

    @Query("SELECT * FROM cache_entries WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getByKey(cacheKey: String): CacheEntryEntity?

    @Upsert
    suspend fun upsert(entry: CacheEntryEntity)

    @Query("DELETE FROM cache_entries WHERE cacheKey = :cacheKey")
    suspend fun deleteByKey(cacheKey: String)

    @Query("DELETE FROM cache_entries")
    suspend fun clearAll()
}
