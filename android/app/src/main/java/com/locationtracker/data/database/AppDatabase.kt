package com.locationtracker.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.locationtracker.LocationTrackerApp
import com.locationtracker.data.model.LocationData

/**
 * Room数据库类
 */
@Database(
    entities = [LocationData::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao

    companion object {
        private const val DATABASE_NAME = "location_tracker.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    LocationTrackerApp().applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2) // 示例迁移
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // 数据库迁移示例（如果需要升级）
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 迁移逻辑
            }
        }
    }
}
