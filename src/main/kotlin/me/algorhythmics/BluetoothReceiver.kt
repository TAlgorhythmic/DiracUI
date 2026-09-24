package me.algorhythmics

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BluetoothReceiver : BroadcastReceiver() {
	companion object {
		private const val TAG = "BluetoothReceiver"

		@Volatile
		var BLUETOOTH_ACTIVE: String? = null
	}

	override fun onReceive(context: Context, intent: Intent) {
		val device = intent.bluetoothDevice()

		when (intent.action) {
			BluetoothDevice.ACTION_ACL_CONNECTED -> {
				val name = device.describe()
				BLUETOOTH_ACTIVE = name

				Log.i(TAG, "connected: $name")

				DiracService.publish(IDiracService.DEVICE_BLUETOOTH, name)
			}

			BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
				Log.i(TAG, "disconnected: ${device.describe()}")

				BLUETOOTH_ACTIVE = null
				DiracService.publish(IDiracService.DEVICE_NONE, null)
			}
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
}
