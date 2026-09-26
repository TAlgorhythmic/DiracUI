package me.algorhythmics.diracui

import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import se.dirac.acs.api.Device
import se.dirac.acs.api.Filter
import se.dirac.acs.api.IAudioControlService
import se.dirac.acs.api.IAudioControlServiceCallback
import se.dirac.acs.api.Output
import se.dirac.acs.api.OutputSettings
import se.dirac.acs.api.Parameter
import se.dirac.acs.api.Usecase
import se.dirac.acs.api.UsecaseItem
import java.util.EnumSet

private const val TAG: String = "Client"
private val INTENT: Intent = Intent().setClassName("se.dirac.acs", "se.dirac.acs.AudioControlService")

@Volatile
var BOUND: IAudioControlService? = null

// Config state
@Volatile
var currentSettings: OutputSettings = OutputSettings(Device.INTERNAL_DEVICE, Filter.INTERNAL_FILTER)
@Volatile
var currentOutput: Output = Output.INTERNAL

private fun loadPreset(name: String, internal: Boolean, insertIfNotPresent: Boolean): OutputSettings {
	val db = App.getInstance().database.writableDatabase
	val values = ContentValues().apply { put("name", name) }

	if (insertIfNotPresent) db.insertWithOnConflict("presets", null, values, SQLiteDatabase.CONFLICT_IGNORE)

	return db.query("presets", null, "name=?", arrayOf(name), null, null, null).use { cursor ->
		cursor.moveToFirst()
		OutputSettings(cursor).apply { if (internal) device.id = -1 }
	}
}

private fun savePreset(name: String) {
	val db = App.getInstance().database.writableDatabase
	val values = ContentValues().apply {
		put("device", currentSettings.device.id)
		put("enabled", currentSettings.enabled)
		put("filterEnabled", currentSettings.filterEnabled)
		put("sfxEnabled", currentSettings.sfxEnabled)
		put("eqEnabled", currentSettings.eqEnabled)
		put("bands", currentSettings.eqBands.joinToString(";"))
		put("stereoWidth", currentSettings.stereoWidth)
		put("tonalBalance", currentSettings.tonalBalance)
		put("loudness", currentSettings.loudness)
		put("usecase", currentSettings.filter.usecase.id)
	}
	db.update("presets", values, "name=?", arrayOf(name))
}

// Load current output db entry and apply it
fun applyUpdatedSettings() {
	val bound = BOUND ?: return

	App.getInstance().worker.execute {
		val internal = !HeadsetReceiver.PLUGGED && BluetoothReceiver.BLUETOOTH_ACTIVE == null
		val presetName = BluetoothReceiver.BLUETOOTH_ACTIVE
			?: if (HeadsetReceiver.PLUGGED) "headphones" else "internal"
		val bundle = Bundle()
		val preset = loadPreset(presetName, internal, presetName != "headphones" && presetName != "internal")

		if (!bound.setOutput2(preset, bundle) ||
			!bound.setParameter(preset.filter.usecase, Parameter.STEREO_WIDTH_ID, preset.stereoWidth, bundle) ||
			!bound.setParameter(preset.filter.usecase, Parameter.TONAL_BALANCE_ID, preset.tonalBalance, bundle) ||
			!bound.setParameter(preset.filter.usecase, Parameter.LOUDNESS_ID, preset.loudness, bundle)
		) { Log.w(TAG, "Service rejected preset '$presetName'") }
		toastError(bundle)

		currentSettings = preset
		MainActivity.getActiveUi()?.apply { runOnUiThread { updateUi() } }
    }
}

fun updateSettings(newSettings: OutputSettings) {
	val bundle = Bundle()
	val bound = BOUND ?: return
	App.getInstance().worker.execute {
		if (bound.setOutput2(newSettings, bundle) &&
			bound.setParameter(newSettings.filter.usecase, Parameter.STEREO_WIDTH_ID, newSettings.stereoWidth, bundle) &&
			bound.setParameter(newSettings.filter.usecase, Parameter.TONAL_BALANCE_ID, newSettings.tonalBalance, bundle) &&
			bound.setParameter(newSettings.filter.usecase, Parameter.LOUDNESS_ID, newSettings.loudness, bundle)
		) {
			val presetName = BluetoothReceiver.BLUETOOTH_ACTIVE
			?: if (HeadsetReceiver.PLUGGED) "headphones" else "internal"
			savePreset(presetName)
			currentSettings = newSettings
			MainActivity.getActiveUi()?.apply { runOnUiThread { updateUi() } }
		}
		toastError(bundle) // Notice if remote error
	}
}

private val CONNECTION: ServiceConnection = object: ServiceConnection {
    override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
		val bind = IAudioControlService.Stub.asInterface(binder)
        BOUND = bind
		Log.i(TAG, "Service bound! Applying settings")

		val instance = App.getInstance()

		instance.worker.execute {
			// Usecases
			instance.internalUsecases.clear()
			instance.externalUsecases.clear()
			val bundle1 = Bundle()
			val internalUsecases = bind.listUsecases(Output.INTERNAL, bundle1)
			val externalUsecases = bind.listUsecases(Output.EXTERNAL, bundle1)
			toastError(bundle1)

			for (internal in internalUsecases)
				instance.internalUsecases[internal.id] = internal
			for (external in externalUsecases)
				instance.externalUsecases[external.id] = external

			// Initialize devices and filters
			instance.devices.clear()
			instance.filters.clear()

			val bundle = Bundle()
			val devices = bind.listDevices2("en", Output.EXTERNAL, bundle)
			val internalDevices = bind.listDevices2("en", Output.INTERNAL, bundle)
			toastError(bundle)

			for (device in devices) {
				instance.devices[device.id] = device
				for (filter in device.filters) {
					if (filter.usecase.getOutput() == Output.INTERNAL)
						filter.usecase = instance.internalUsecases[filter.usecase.id]
							?: instance.internalUsecases[Usecase.INTERNAL_POWERSOUND.value] as UsecaseItem
					else filter.usecase = instance.externalUsecases[filter.usecase.id]
							?: instance.externalUsecases[Usecase.EXTERNAL_HEADSET.value] as UsecaseItem
					instance.filters[filter.id] = filter
				}
			}
			for (device in internalDevices) {
				instance.devices[device.id] = device
				Device.INTERNAL_DEVICE = device
				for (filter in device.filters) {
					if (filter.usecase.getOutput() == Output.INTERNAL)
						filter.usecase = internalUsecases[filter.usecase.id]
							?: instance.internalUsecases[Usecase.INTERNAL_POWERSOUND.value] as UsecaseItem
					else filter.usecase = externalUsecases[filter.usecase.id]
							?: instance.externalUsecases[Usecase.EXTERNAL_HEADSET.value] as UsecaseItem
					instance.filters[filter.id] = filter
					Filter.INTERNAL_FILTER = filter
				}
			}

			val bundle2 = Bundle()
			bind.registerCallback2(serviceCallback, bundle2)
			toastError(bundle2)

			applyUpdatedSettings()
		}
	}

	override fun onServiceDisconnected(p0: ComponentName) {
		BOUND = null
		Log.i(TAG, "Service disconnected, Reconnecting")
		App.getInstance().unbindService(CONNECTION)
		bindService(App.getInstance())
	}

	override fun onNullBinding(name: ComponentName?) {
		super.onNullBinding(name)
		Log.w(TAG, "Null binding received")
	}

	override fun onBindingDied(name: ComponentName?) {
        super.onBindingDied(name)
		Log.w(TAG, "Bound service died for some reason? Reconnecting")
		BOUND = null
		App.getInstance().unbindService(CONNECTION)
		bindService(App.getInstance())
    }
}

private val serviceCallback = object: IAudioControlServiceCallback.Stub() {
	override fun onFilterAdd(j: Long, iArr: IntArray?) {/* Unused */}
	override fun onRoutingChanged(i: Int) {/* Unused */}
	override fun onSetUser(str: String?) {/* Unused */}
	override fun onSyncDone() {/* Unused */}

	override fun onSettingsChanged(output: Output?, outputSettings: OutputSettings?) {
		val old = currentSettings

		if (output != null)
			currentOutput = output
		if (outputSettings != null)
			currentSettings = outputSettings
		currentSettings.stereoWidth = old.stereoWidth
		currentSettings.tonalBalance = old.tonalBalance
		currentSettings.loudness = old.loudness

		// Update UI if active
		val ui = MainActivity.getActiveUi()
		ui?.runOnUiThread { ui.updateUi() }
	}
}

fun bindService(ctx: Context): Boolean {
	try {
		Log.i(TAG, "Binding to service...")
		return ctx.bindService(INTENT, CONNECTION, Context.BIND_AUTO_CREATE)
	} catch (e: Exception) {
		Log.e(TAG, "Failed to bind service", e)
		return false
	}
}

fun isConnected(): Boolean {
	val bound = BOUND // Prevent mutation while in use
	return bound != null && bound.asBinder().isBinderAlive
}
