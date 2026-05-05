package id.pbbku.mobileportal.data.local.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_entries")
data class CacheEntryEntity(
    @PrimaryKey val cacheKey: String,
    val payloadJson: String,
    val sourceLabel: String,
    val updatedAtEpochMillis: Long,
)
