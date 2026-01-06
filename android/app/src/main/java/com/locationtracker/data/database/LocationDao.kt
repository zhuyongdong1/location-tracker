package com.locationtracker.data.database

import androidx.room.*
import com.locationtracker.data.model.LocationData
import kotlinx.coroutines.flow.Flow

/**
 * 位置数据DAO接口
 */
@Dao
interface LocationDao {

    /**
     * 插入位置数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(locationData: LocationData): Long

    /**
     * 批量插入位置数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locationDataList: List<LocationData>)

    /**
     * 更新位置数据
     */
    @Update
    suspend fun updateLocation(locationData: LocationData)

    /**
     * 根据ID删除位置数据
     */
    @Delete
    suspend fun deleteLocation(locationData: LocationData)

    /**
     * 删除指定ID的数据
     */
    @Query("DELETE FROM location_data WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 获取所有待上报的位置数据（按时间升序）
     */
    @Query("SELECT * FROM location_data WHERE uploadStatus = 0 ORDER BY createdAt ASC")
    suspend fun getPendingLocations(): List<LocationData>

    /**
     * 获取最近一次成功上报的位置数据
     */
    @Query("SELECT * FROM location_data WHERE uploadStatus = 1 ORDER BY timestampServer DESC LIMIT 1")
    suspend fun getLastSuccessfulLocation(): LocationData?

    /**
     * 获取今天的位置数据数量
     */
    @Query("SELECT COUNT(*) FROM location_data WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodayLocationCount(startOfDay: Long, endOfDay: Long): Int

    /**
     * 获取最近7天的位置数据统计
     */
    @Query("""
        SELECT
            DATE(createdAt / 1000, 'unixepoch', 'localtime') as date,
            COUNT(*) as count,
            AVG(uploadStatus) as successRate
        FROM location_data
        WHERE createdAt >= :sevenDaysAgo
        GROUP BY DATE(createdAt / 1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """)
    suspend fun getWeeklyStats(sevenDaysAgo: Long): List<DailyStats>

    /**
     * 删除N天前的数据（数据清理）
     */
    @Query("DELETE FROM location_data WHERE createdAt < :cutoffTime")
    suspend fun deleteOldData(cutoffTime: Long): Int

    /**
     * 清空所有位置数据
     */
    @Query("DELETE FROM location_data")
    suspend fun clearAllLocations()

    /**
     * 获取数据库中的总记录数
     */
    @Query("SELECT COUNT(*) FROM location_data")
    suspend fun getTotalCount(): Int

    /**
     * 获取失败的上报记录
     */
    @Query("SELECT * FROM location_data WHERE uploadStatus = 2 ORDER BY updatedAt DESC LIMIT 10")
    suspend fun getFailedUploads(): List<LocationData>
}

/**
 * 每日统计数据类
 */
data class DailyStats(
    val date: String,
    val count: Int,
    val successRate: Float
)
