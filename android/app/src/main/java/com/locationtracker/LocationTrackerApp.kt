package com.locationtracker

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.locationtracker.worker.LocationTrackerWorkerFactory

class LocationTrackerApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        // 初始化WorkManager
        WorkManager.initialize(this, workManagerConfiguration)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(LocationTrackerWorkerFactory())
            .build()
    }
}
