package me.algorhythmics

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val device = intent.bluetoothDevice()

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED ->
                Log.i(TAG, "connected: ${device.describe()}")

            BluetoothDevice.ACTION_ACL_DISCONNECTED ->
                Log.i(TAG, "disconnected: ${device.describe()}")
        }
    }

    @Suppress("DEPRECATION")
    private fun Intent.bluetoothDevice(): BluetoothDevice? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

    // getName() needs BLUETOOTH_CONNECT from API 31 on; fall back to the address.
    private fun BluetoothDevice?.describe(): String {
        if (this == null) return "unknown"

        return try {
            "$name ($address)"
        } catch (e: SecurityException) {
            address
        }
    }

    private companion object {
        const val TAG = "BluetoothReceiver"
    }
}
