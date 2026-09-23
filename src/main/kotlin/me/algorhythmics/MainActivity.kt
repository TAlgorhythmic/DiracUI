package me.algorhythmics

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import se.dirac.acs.api.Device
import se.dirac.acs.api.Filter

import se.dirac.acs.api.IAudioControlServiceCallback
import se.dirac.acs.api.Output
import se.dirac.acs.api.OutputSettings

class MainActivity : Activity() {
	// UI handles
	private lateinit var diracEnabled: Switch
	private lateinit var filterEnabled: Switch
	private lateinit var sfxEnabled: Switch
	private lateinit var eqEnabled: Switch

	private val headsetReceiver = HeadsetReceiver()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		activeUi = this
		val elements = composeUi()

		// Init UI handles
		diracEnabled = elements.diracEnabled
		filterEnabled = elements.filterEnabled
		sfxEnabled = elements.sfxEnabled
		eqEnabled = elements.eqEnabled

		setContentView(elements.view)
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
		activeUi = null
	}

	// Below API 31 the install-time BLUETOOTH permission is enough. From 31 on
	// the ACL broadcasts are not delivered at all until this is granted.
	private fun requestBluetoothPermission() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

		if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH)
		}
	}

	private companion object {
		const val REQUEST_BLUETOOTH = 1

		// Config state
		@Volatile
		var currentSettings: OutputSettings = OutputSettings(Device.NOTHING_DEVICE, Filter.NOTHING_FILTER)
		@Volatile
		var currentOutput: Output = Output.INTERNAL
		@Volatile
		var activeUi: MainActivity? = null

		private val serviceCallback = object: IAudioControlServiceCallback.Stub() {
			override fun onFilterAdd(j: Long, iArr: IntArray?) {/* Unused */}
			override fun onRoutingChanged(i: Int) {/* Unused */}
			override fun onSetUser(str: String?) {/* Unused */}
			override fun onSyncDone() {/* Unused */}

			override fun onSettingsChanged(output: Output, outputSettings: OutputSettings) {
				currentOutput = output
				currentSettings = outputSettings

				// Update UI if active
				val ui = activeUi
                ui?.runOnUiThread {
                    ui.diracEnabled.isChecked = currentSettings.enabled
                    ui.diracEnabled.isEnabled = true
                    ui.filterEnabled.isChecked = currentSettings.filterEnabled
                    ui.filterEnabled.isEnabled = true
                    ui.sfxEnabled.isChecked = currentSettings.sfxEnabled
                    ui.sfxEnabled.isEnabled = true
                    ui.eqEnabled.isChecked = currentSettings.eqEnabled
                    ui.eqEnabled.isEnabled = true
                }
			}
		}
	}
}
