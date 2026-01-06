package com.locationtracker.ui.main

import android.Manifest
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.locationtracker.worker.LocationReportWorker
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // 上报开关状态
    private val _isReportingEnabled = MutableLiveData<Boolean>()
    val isReportingEnabled: LiveData<Boolean> = _isReportingEnabled

    // 消息提示
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        checkReportingStatus()
    }

    /**
     * 检查必需权限
     */
    fun hasRequiredPermissions(): Boolean {
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
     * 设置位置上报开关
     */
    fun setLocationReportingEnabled(enabled: Boolean) {
        if (enabled) {
            LocationReportWorker.scheduleLocationReport(context)
            _message.value = "位置上报已开启"
        } else {
            LocationReportWorker.cancelLocationReport(context)
            _message.value = "位置上报已关闭"
        }
        _isReportingEnabled.value = enabled
    }

    /**
     * 复制设备信息
     */
    fun copyDeviceInfo() {
        try {
            val deviceInfo = buildDeviceInfo()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("设备信息", deviceInfo)
            clipboard.setPrimaryClip(clip)
            _message.value = "设备信息已复制到剪贴板"
        } catch (e: Exception) {
            _message.value = "复制设备信息失败: ${e.message}"
        }
    }

    /**
     * 检查上报任务状态
     */
    private fun checkReportingStatus() {
        val isScheduled = LocationReportWorker.isLocationReportScheduled(context)
        _isReportingEnabled.value = isScheduled
    }

    /**
     * 构建设备信息字符串
     */
    private fun buildDeviceInfo(): String {
        val androidId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )

        val deviceInfo = StringBuilder()
        deviceInfo.append("设备ID: $androidId\n")
        deviceInfo.append("Android版本: ${android.os.Build.VERSION.RELEASE}\n")
        deviceInfo.append("设备型号: ${android.os.Build.MODEL}\n")
        deviceInfo.append("制造商: ${android.os.Build.MANUFACTURER}\n")

        // 权限状态
        deviceInfo.append("精确定位权限: ${hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)}\n")
        deviceInfo.append("粗略定位权限: ${hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)}\n")
        deviceInfo.append("后台定位权限: ${hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)}\n")

        // 定位服务状态
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        deviceInfo.append("GPS启用: ${locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)}\n")
        deviceInfo.append("网络定位启用: ${locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)}\n")

        return deviceInfo.toString()
    }

    private fun hasPermission(permission: String): String {
        return if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            "已授权"
        } else {
            "未授权"
        }
    }
}
