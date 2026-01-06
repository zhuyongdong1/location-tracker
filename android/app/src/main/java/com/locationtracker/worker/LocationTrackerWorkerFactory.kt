package com.locationtracker.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

/**
 * 自定义Worker工厂
 */
class LocationTrackerWorkerFactory : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {

        return when (workerClassName) {
            LocationReportWorker::class.java.name -> {
                LocationReportWorker(appContext, workerParameters)
            }
            else -> null
        }
    }
}
