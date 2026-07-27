package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.updateAll
import com.example.data.repository.PriceRepository
import com.example.widget.PriceWidget
import kotlinx.coroutines.*

class PriceUpdateService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var updateJob: Job? = null
    private lateinit var repository: PriceRepository

    companion object {
        private const val TAG = "PriceUpdateService"
        private const val CHANNEL_ID = "price_updates_channel"
        private const val NOTIFICATION_ID = 101
        
        fun startService(context: Context) {
            val intent = Intent(context, PriceUpdateService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service", e)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PriceUpdateService::class.java)
            try {
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop service", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = PriceRepository(applicationContext)
        createNotificationChannel()
        startServiceForeground()
        startPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startServiceForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("نرخ لحظه‌ای طلا و ارز")
            .setContentText("در حال به‌روزرسانی قیمت‌های طلا و ارز...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
            // Safe fallback starting
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "به‌روزرسانی نرخ‌ها",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "نمایش نوتیفیکیشن سرویس پس‌زمینه برای دریافت قیمت‌های طلا و ارز"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startPolling() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                try {
                    Log.d(TAG, "Fetching fresh prices inside background service...")
                    repository.fetchFreshPrices()
                    
                    // Force update widgets
                    try {
                        PriceWidget().updateAll(applicationContext)
                        Log.d(TAG, "Glance widgets triggered from background service successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating widgets from service loop", e)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Background polling exception", e)
                }
                
                // Poll exactly every 60 seconds (1 minute)
                delay(60000L)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        serviceScope.cancel()
        Log.d(TAG, "Foreground service destroyed, background polling suspended.")
    }
}
