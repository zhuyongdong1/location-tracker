package com.locationtracker.data.api

import com.locationtracker.data.model.LocationApiRequest
import com.locationtracker.data.model.LocationApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 位置API服务接口
 */
interface LocationApiService {

    /**
     * 上报位置数据
     */
    @POST("api/v1/location/push")
    suspend fun pushLocation(
        @Header("Authorization") token: String,
        @Body request: LocationApiRequest
    ): Response<LocationApiResponse>

    /**
     * 获取最新位置
     */
    @GET("api/v1/location/latest")
    suspend fun getLatestLocation(
        @Header("Authorization") token: String,
        @Query("device_id") deviceId: String? = null
    ): Response<LocationApiResponse>

    /**
     * 获取历史轨迹
     */
    @GET("api/v1/location/list")
    suspend fun getLocationList(
        @Header("Authorization") token: String,
        @Query("from") from: Long,
        @Query("to") to: Long,
        @Query("device_id") deviceId: String? = null,
        @Query("limit") limit: Int = 100
    ): Response<LocationApiResponse>

    /**
     * 删除位置数据
     */
    @POST("api/v1/location/delete")
    suspend fun deleteLocations(
        @Header("Authorization") token: String,
        @Body deleteRequest: DeleteRequest
    ): Response<LocationApiResponse>
}

/**
 * 删除请求数据类
 */
data class DeleteRequest(
    val from: Long,
    val to: Long,
    val device_id: String? = null
)
