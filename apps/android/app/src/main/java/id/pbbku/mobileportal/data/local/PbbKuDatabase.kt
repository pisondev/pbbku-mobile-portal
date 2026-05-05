package id.pbbku.mobileportal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.pbbku.mobileportal.data.local.cache.CacheEntryDao
import id.pbbku.mobileportal.data.local.cache.CacheEntryEntity
import id.pbbku.mobileportal.data.local.report.ReportDraftDao
import id.pbbku.mobileportal.data.local.report.ReportDraftEntity

@Database(
    entities = [
        CacheEntryEntity::class,
        ReportDraftEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(PbbKuTypeConverters::class)
abstract class PbbKuDatabase : RoomDatabase() {
    abstract fun cacheEntryDao(): CacheEntryDao

    abstract fun reportDraftDao(): ReportDraftDao

    companion object {
        fun create(context: Context): PbbKuDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PbbKuDatabase::class.java,
                "pbbku.db",
            ).build()
        }
    }
}
