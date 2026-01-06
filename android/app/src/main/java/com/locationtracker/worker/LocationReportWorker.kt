package com.locationtracker.worker

import android.content.Context
import androidx.work.*
import com.locationtracker.data.repository.LocationRepository
import com.locationtracker.service.LocationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * 位置上报Worker
 */
class LocationReportWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val locationService = LocationService(context)
    private val locationRepository = LocationRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. 获取当前位置
            val locationResult = locationService.getCurrentLocation()

            if (locationResult.isFailure) {
                logError("定位失败: ${locationResult.exceptionOrNull()?.message}")
                return@withContext Result.failure()
            }

            val locationData = locationResult.getOrNull()!!

            // 2. 直接上报到服务器（精简版：不上报就拉倒）
            val uploadResult = locationRepository.uploadLocation(locationData)

            if (uploadResult.isSuccess) {
                logInfo("位置数据上报成功")
                Result.success()
            } else {
                logError("位置数据上报失败: ${uploadResult.exceptionOrNull()?.message}")
                Result.failure() // 失败了就失败了，不重试
            }

        } catch (e: Exception) {
            logError("Worker执行异常: ${e.message}")
            Result.failure()
        }
    }

    private fun logInfo(message: String) {
        android.util.Log.i(TAG, message)
    }

    private fun logError(message: String) {
        android.util.Log.e(TAG, message)
    }

    companion object {
        private const val TAG = "LocationReportWorker"

        // Worker名称
        const val WORK_NAME = "location_report_work"

        /**
         * 调度位置上报任务
         */
        fun scheduleLocationReport(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // 需要网络连接
                .build()

            val workRequest = PeriodicWorkRequestBuilder<LocationReportWorker>(
                30, TimeUnit.MINUTES // 30分钟间隔
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    5, TimeUnit.MINUTES // 指数退避，最小5分钟
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, // 替换现有任务
                workRequest
            )
        }

        /**
         * 取消位置上报任务
         */
        fun cancelLocationReport(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * 检查位置上报任务是否在运行
         */
        fun isLocationReportScheduled(context: Context): Boolean {
            val workManager = WorkManager.getInstance(context)
            val workInfos = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()

            return workInfos.any { workInfo ->
                workInfo.state == WorkInfo.State.RUNNING ||
                workInfo.state == WorkInfo.State.ENQUEUED
            }
        }
    }
}
