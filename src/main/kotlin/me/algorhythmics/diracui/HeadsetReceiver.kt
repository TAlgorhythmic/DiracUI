package me.algorhythmics.diracui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log


class HeadsetReceiver : BroadcastReceiver() {
	companion object {
		private const val TAG = "HeadsetReceiver"

		@Volatile
		var PLUGGED: Boolean = false
	}

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AudioManager.ACTION_HEADSET_PLUG) return

        val plugged = intent.getIntExtra("state", 0) == 1
        val name = intent.getStringExtra("name") ?: "headset"
        val hasMic = intent.getIntExtra("microphone", 0) == 1

        Log.i(TAG, "$name ${if (plugged) "plugged" else "unplugged"} (mic=$hasMic)")

		PLUGGED = plugged
		if (BluetoothReceiver.BLUETOOTH_ACTIVE == null)
			applyUpdatedSettings()
    }
}
