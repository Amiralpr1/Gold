package com.example.service

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.example.data.repository.PriceRepository
import com.example.widget.PriceWidget
import java.util.concurrent.TimeUnit

/**
 * PriceUpdateWorker is a periodic WorkManager worker that serves as a fallback sync backstop.
 * 
 * Why do we use both a Foreground Service and a WorkManager Worker?
 * 1. PERSISTENT FREQUENCY: The user requires a strict 60-second update cycle. Android's standard background
 *    WorkManager allows a minimum interval of 15 minutes to preserve battery.
 * 2. POWER MANAGEMENT (DOZE MODE): On Android 10+, if the device remains idle, the OS enters Doze Mode.
 *    This may suspend foreground services or clamp network sockets. WorkManager uses internal system-level
 *    scheduling slots to bypass some limits, serving as a reliable sync backstop.
 * 3. CRASH RECOVERY: If the persistent service is terminated under high-pressure system memory conditions,
 *    WorkManager acts as a reliable daemon that restarts our price synchronization and keeps the home screen
 *    widgets updated.
 */
class PriceUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "PriceUpdateWorker"
        private const val WORK_NAME = "price_periodic_update_work"

        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<PriceUpdateWorker>(
                15, TimeUnit.MINUTES // Minimum permitted Android background execution period
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Retain existing, do not restart
                periodicRequest
            )
            Log.d(TAG, "Fallback WorkManager periodic update scheduled.")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "WorkManager periodic backstop task started executing...")
        val repository = PriceRepository(applicationContext)
        return try {
            repository.fetchFreshPrices()
            
            // Re-sync Glance widgets immediately with the freshly fetched data
            try {
                PriceWidget().updateAll(applicationContext)
                Log.d(TAG, "Glance widgets successfully updated inside Worker")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets from Worker", e)
            }

            // Also ensure our foreground service is alive and healthy!
            try {
                PriceUpdateService.startService(applicationContext)
            } catch (e: Exception) {
                Log.e(TAG, "Error ensuring service is running from Worker", e)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "WorkManager background fetch failed", e)
            Result.retry()
        }
    }
}
