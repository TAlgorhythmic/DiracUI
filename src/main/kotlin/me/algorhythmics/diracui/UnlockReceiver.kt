package me.algorhythmics.diracui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG: String = "UnlockReceiver"

class UnlockReceiver : BroadcastReceiver() {
	/** Triggers when user first unlocks, so that storage is decrypted, hence can be accessed by dirac */
    override fun onReceive(ctx: Context, intent: Intent) {
		if (intent.action != Intent.ACTION_USER_UNLOCKED) return
		Log.i(TAG, "User unlock received, binding...")

		bindService(App.getInstance())
    }
}
