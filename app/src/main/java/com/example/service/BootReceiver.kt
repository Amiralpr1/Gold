package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        val action = intent.action
        Log.d("BootReceiver", "Received broadcast signal: $action")
        
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            try {
                PriceUpdateService.startService(context)
                PriceUpdateWorker.schedulePeriodicWork(context)
                Log.d("BootReceiver", "Successfully restarted background polling service and scheduled WorkManager periodic backup on boot.")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to start background synchronization systems on boot", e)
            }
        }
    }
}
