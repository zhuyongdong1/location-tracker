package com.locationtracker.data.repository

import com.locationtracker.data.api.ApiClient
import com.locationtracker.data.api.LocationApiService
import com.locationtracker.data.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 位置数据仓库 - 精简版
 * 只保留最核心的上报功能，去掉缓存补发机制
 */
class LocationRepository(
    private val apiService: LocationApiService = ApiClient.createLocationService("your_token_here")
) {

    /**
     * 上报位置数据到服务器
     * 简化版：直接上报，失败就失败，不缓存不补发
     */
    suspend fun uploadLocation(locationData: LocationData): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.pushLocation(
                token = "Bearer your_token_here", // 固定Token
                request = locationData.toApiRequest()
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("上传失败: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
