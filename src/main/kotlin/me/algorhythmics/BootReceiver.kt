package me.algorhythmics

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Fires once the device has booted, provided the app has been launched at least
 * once since install - a force-stopped app is excluded from this broadcast on
 * API 24+.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // Roughly 10 seconds of runway here before the receiver is torn down.
        // Anything longer belongs in goAsync() or a JobScheduler job.
        Log.i(TAG, "boot completed")
    }

    private companion object {
        const val TAG = "BootReceiver"
    }
}
