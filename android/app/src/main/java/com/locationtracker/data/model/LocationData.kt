package com.locationtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 位置数据实体类
 */
@Entity(tableName = "location_data")
data class LocationData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 设备标识
    val deviceId: String,

    // 客户端采集时间戳（毫秒）
    val timestampClient: Long,

    // 服务端入库时间戳（毫秒）- 上报成功后更新
    var timestampServer: Long? = null,

    // 地理位置
    val latitude: Double,
    val longitude: Double,

    // 定位精度（米）
    val accuracy: Float,

    // 定位提供者
    val provider: String? = null,

    // 设备电量百分比
    val batteryLevel: Int? = null,

    // 备注信息
    val remark: String? = null,

    // 上报状态：0-待上报，1-上报成功，2-上报失败
    var uploadStatus: Int = 0,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 最后更新时间
    var updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 转换为API请求格式
     */
    fun toApiRequest(): LocationApiRequest {
        return LocationApiRequest(
            device_id = deviceId,
            ts_client = timestampClient,
            lat = latitude,
            lng = longitude,
            accuracy_m = accuracy.toDouble(),
            provider = provider,
            battery_pct = batteryLevel,
            remark = remark
        )
    }
}

/**
 * API请求数据类
 */
data class LocationApiRequest(
    @SerializedName("device_id")
    val device_id: String,

    @SerializedName("ts_client")
    val ts_client: Long,

    @SerializedName("lat")
    val lat: Double,

    @SerializedName("lng")
    val lng: Double,

    @SerializedName("accuracy_m")
    val accuracy_m: Double,

    @SerializedName("provider")
    val provider: String? = null,

    @SerializedName("battery_pct")
    val battery_pct: Int? = null,

    @SerializedName("remark")
    val remark: String? = null
)

/**
 * API响应数据类
 */
data class LocationApiResponse(
    val code: Int,
    val message: String,
    val data: LocationResponseData?
)

data class LocationResponseData(
    val id: Long,
    val ts_server: Long
)
