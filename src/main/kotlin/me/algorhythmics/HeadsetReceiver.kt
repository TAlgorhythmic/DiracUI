package me.algorhythmics

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log

/**
 * Wired headset plug/unplug. Registered at runtime by [MainActivity] - this
 * broadcast is FLAG_RECEIVER_REGISTERED_ONLY and never reaches a manifest
 * receiver. Needs no permission.
 */
class HeadsetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AudioManager.ACTION_HEADSET_PLUG) return

        val plugged = intent.getIntExtra("state", 0) == 1
        val name = intent.getStringExtra("name") ?: "headset"
        val hasMic = intent.getIntExtra("microphone", 0) == 1

        Log.i(TAG, "$name ${if (plugged) "plugged" else "unplugged"} (mic=$hasMic)")

        if (plugged) {
            DiracService.publish(IDiracService.DEVICE_WIRED, name)
        } else {
            DiracService.publish(IDiracService.DEVICE_NONE, null)
        }
    }

    private companion object {
        const val TAG = "HeadsetReceiver"
    }
}
