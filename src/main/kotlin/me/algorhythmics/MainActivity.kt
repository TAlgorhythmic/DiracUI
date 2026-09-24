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

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

		if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH)
		}
	}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == REQUEST_BLUETOOTH) {
			
		}
    }

	override fun onStart() {
		super.onStart()
	}

	override fun onStop() {
		super.onStop()
		activeUi = null
	}

	fun updateUi(settings: OutputSettings, output: Output) {
		diracEnabled.isChecked = settings.enabled
		diracEnabled.isEnabled = true
		filterEnabled.isChecked = settings.filterEnabled
		filterEnabled.isEnabled = true
		sfxEnabled.isChecked = settings.sfxEnabled
		sfxEnabled.isEnabled = true
		eqEnabled.isChecked = settings.eqEnabled
		eqEnabled.isEnabled = true
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
