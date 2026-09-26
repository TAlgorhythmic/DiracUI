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
	private lateinit var elements: UiElements

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Init UI handles
		elements = composeUi()
		setContentView(elements.view)

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

		if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH)
		}
	}

	override fun onStart() {
		super.onStart()
		activeUi = this
		updateUi()
	}

	override fun onStop() {
		super.onStop()
		activeUi = null
	}

	fun updateUi() {
		updating = true // Prevent the refresh from triggering more refreshes

		elements.diracEnabled.isChecked = currentSettings.enabled
		elements.diracEnabled.isEnabled = true
		elements.filterEnabled.isChecked = currentSettings.filterEnabled
		elements.filterEnabled.isEnabled = currentSettings.device.filterAvailable
		elements.sfxEnabled.isChecked = currentSettings.sfxEnabled
		elements.sfxEnabled.isEnabled = currentSettings.filter.sfxAvailable
		elements.eqEnabled.isChecked = currentSettings.eqEnabled
		elements.eqEnabled.isEnabled = currentSettings.filter.bandCount > 0
		if (currentSettings.filter.bandCount > 0) elements.bands.update(currentSettings.eqBands)
		elements.bands.isEnabled = currentSettings.eqEnabled && currentSettings.filter.bandCount > 0
		elements.stereoWidth.progress = (25f * (currentSettings.stereoWidth + 1f)).toInt()
		elements.tonalBalance.progress = (25f * (currentSettings.tonalBalance + 1f)).toInt()
		elements.loudness.progress = (25f * (currentSettings.loudness + 1f)).toInt()

		val instance = App.getInstance()
		val external = currentSettings.device.id >= 0

		elements.device.isEnabled = external
		if (external)
			elements.device.update(instance.devices.values.filter {dev -> dev.id >= 0}.toList(), currentSettings.device)

		updating = false
	}

	companion object {
		const val REQUEST_BLUETOOTH = 1

		@Volatile
		private var activeUi: MainActivity? = null

		fun getActiveUi(): MainActivity? {
			return activeUi
		}
	}
}
