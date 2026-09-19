package me.algorhythmics

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    private val headsetReceiver = HeadsetReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val label = TextView(this)
        label.text = "Hello world!"

        setContentView(label)

        requestBluetoothPermission()
    }

    override fun onStart() {
        super.onStart()

        // ACTION_HEADSET_PLUG is FLAG_RECEIVER_REGISTERED_ONLY, so it has to be
        // registered here rather than in the manifest. It is sticky: registering
        // delivers the current plug state immediately.
        val filter = IntentFilter(AudioManager.ACTION_HEADSET_PLUG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(headsetReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(headsetReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()

        unregisterReceiver(headsetReceiver)
    }

    // Below API 31 the install-time BLUETOOTH permission is enough. From 31 on
    // the ACL broadcasts are not delivered at all until this is granted.
    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH)
        }
    }

    private companion object {
        const val REQUEST_BLUETOOTH = 1
    }
}
