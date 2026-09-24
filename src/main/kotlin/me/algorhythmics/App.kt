package me.algorhythmics

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.media.AudioManager
import android.os.Build
import se.dirac.acs.api.Device
import se.dirac.acs.api.Filter

class App : Application() {
	companion object {
		private lateinit var instance: App

		fun getInstance(): App {
			return instance
		}
	}

	val devices: HashMap<Long, Device> = HashMap()
	val filters: HashMap<Long, Filter> = HashMap()

	lateinit var database: DbHelper

    override fun onCreate() {
        super.onCreate()
		instance = this
		database = DbHelper(this)

		devices[-1] = Device.NOTHING_DEVICE
		filters[-1] = Filter.INTERNAL_FILTER

		// Register headset receiver
		val headsetFilter = IntentFilter(AudioManager.ACTION_HEADSET_PLUG)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(HeadsetReceiver(), headsetFilter, Context.RECEIVER_NOT_EXPORTED)
		} else {
			registerReceiver(HeadsetReceiver(), headsetFilter)
		}

		if (hasBluetoothPerm()) {
			val filter = IntentFilter().apply {
				addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
				addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
			}
			registerReceiver(BluetoothReceiver(), filter)
		}

		// Register user unlock receiver
		registerReceiver(UnlockReceiver(), IntentFilter(Intent.ACTION_USER_UNLOCKED))
    }

	fun hasBluetoothPerm(): Boolean {
		return (
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
			checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
		) || Build.VERSION.SDK_INT < Build.VERSION_CODES.S
	}
}
