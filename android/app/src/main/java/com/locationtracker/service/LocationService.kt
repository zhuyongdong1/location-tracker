package com.locationtracker.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.locationtracker.data.model.LocationData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 定位服务类
 */
class LocationService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * 获取当前位置（一次性）
     * 使用平衡精度模式，省电优先
     */
    suspend fun getCurrentLocation(): Result<LocationData> = suspendCancellableCoroutine { continuation ->
        // 检查权限
        if (!hasLocationPermissions()) {
            continuation.resume(Result.failure(SecurityException("缺少定位权限")))
            return@suspendCancellableCoroutine
        }

        // 创建定位请求
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY // 平衡精度，省电
            maxWaitTime = 10000 // 最大等待时间10秒
            interval = 0 // 一次性请求
            fastestInterval = 0
            numUpdates = 1 // 只获取一次
        }

        // 定位回调
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val locationData = createLocationData(location)
                    continuation.resume(Result.success(locationData))
                } else {
                    continuation.resume(Result.failure(Exception("无法获取位置信息")))
                }
                fusedLocationClient.removeLocationUpdates(this)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    continuation.resume(Result.failure(Exception("位置服务不可用")))
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        // 请求定位更新
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            ).addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
        } catch (e: SecurityException) {
            continuation.resume(Result.failure(e))
        }

        // 处理协程取消
        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * 监听位置变化（用于持续监控）
     */
    fun observeLocationUpdates(): Flow<LocationData> = callbackFlow {
        if (!hasLocationPermissions()) {
            close(SecurityException("缺少定位权限"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            interval = 30 * 60 * 1000 // 30分钟
            fastestInterval = 10 * 60 * 1000 // 最快10分钟
            maxWaitTime = 5 * 60 * 1000 // 最大等待5分钟
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val locationData = createLocationData(location)
                    trySend(locationData)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * 检查定位权限
     */
    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 从Location对象创建LocationData
     */
    private fun createLocationData(location: Location): LocationData {
        // 获取设备信息
        val deviceId = getDeviceId()
        val batteryLevel = getBatteryLevel()

        return LocationData(
            deviceId = deviceId,
            timestampClient = location.time,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            provider = location.provider,
            batteryLevel = batteryLevel,
            remark = if (location.accuracy > 100) "低精度定位" else null
        )
    }

    /**
     * 获取设备ID
     */
    private fun getDeviceId(): String {
        // 使用Android ID作为设备标识
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }

    /**
     * 获取电池电量
     */
    private fun getBatteryLevel(): Int? {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查GPS是否启用
     */
    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }

    /**
     * 检查网络定位是否启用
     */
    fun isNetworkEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }
}
