package me.algorhythmics

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
import se.dirac.acs.api.Usecase
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
		put("filter", currentSettings.filter.id)
		put("device", currentSettings.device.id)
		put("enabled", currentSettings.enabled)
		put("filterEnabled", currentSettings.filterEnabled)
		put("sfxEnabled", currentSettings.sfxEnabled)
		put("eqEnabled", currentSettings.eqEnabled)
		put("band0", currentSettings.eqBands[0])
		put("band1", currentSettings.eqBands[1])
		put("band2", currentSettings.eqBands[2])
		put("band3", currentSettings.eqBands[3])
		put("band4", currentSettings.eqBands[4])
		put("band5", currentSettings.eqBands[5])
		put("band6", currentSettings.eqBands[6])
	}
	db.update("presets", values, "name=?", arrayOf(name))
}

fun applyUpdatedSettings() {
	val bound = BOUND ?: return

	val internal = !HeadsetReceiver.PLUGGED && BluetoothReceiver.BLUETOOTH_ACTIVE == null
	val presetName = BluetoothReceiver.BLUETOOTH_ACTIVE
		?: if (HeadsetReceiver.PLUGGED) "headphones" else "internal"

	val bundle = Bundle()
	if (!bound.setOutput2(loadPreset(presetName, internal, presetName != "headphones" && presetName != "internal"), bundle))
		Log.w(TAG, "Service rejected preset '$presetName'")
	toastError(bundle)
}

fun updateSettings(newSettings: OutputSettings): Boolean {
	val bundle = Bundle()
	val bound = BOUND ?: return false
	if (!bound.setOutput2(newSettings, bundle)) return false

	val presetName = BluetoothReceiver.BLUETOOTH_ACTIVE
		?: if (HeadsetReceiver.PLUGGED) "headphones" else "internal"
	savePreset(presetName)
	toastError(bundle) // Notice if remote error

	return true
}

private val CONNECTION = object: ServiceConnection {
    override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
		val bind = IAudioControlService.Stub.asInterface(binder)
        BOUND = bind
		Log.i(TAG, "Service bound! Applying settings")

		// Initialize devices and filters
		val instance = App.getInstance()
		instance.devices.clear()
		instance.filters.clear()

		val bundle = Bundle()
		val devices = bind.listDevices2("en", Output.EXTERNAL, bundle)
		val internalDevices = bind.listDevices2("en", Output.INTERNAL, bundle)
		toastError(bundle)

		for (device in devices) {
            instance.devices[device.id] = device
            for (filter in device.filters) {
                instance.filters[filter.id] = filter
            }
		}
		for (device in internalDevices) {
            instance.devices[device.id] = device
			Device.INTERNAL_DEVICE = device
            for (filter in device.filters) {
                instance.filters[filter.id] = filter
				Filter.INTERNAL_FILTER = filter
            }
		}

		// Usecases
		instance.internalUsecases.clear()
		instance.externalUsecases.clear()
		val bundle1 = Bundle()
		val internalUsecases = bind.listUsecases(Output.INTERNAL, bundle1)
		val externalUsecases = bind.listUsecases(Output.EXTERNAL, bundle1)
		toastError(bundle)

		for (internal in internalUsecases)
			instance.internalUsecases[internal.id] = internal
		for (external in externalUsecases)
			instance.externalUsecases[external.id] = external

		applyUpdatedSettings()
    }

    override fun onServiceDisconnected(p0: ComponentName) {
        BOUND = null
		Log.i(TAG, "Service disconnected")
    }

    override fun onNullBinding(name: ComponentName?) {
        super.onNullBinding(name)
		Log.w(TAG, "Null binding received")
    }

    override fun onBindingDied(name: ComponentName?) {
        super.onBindingDied(name)
		Log.w(TAG, "Bound service died for some reason?")
		BOUND = null
    }
}

private val serviceCallback = object: IAudioControlServiceCallback.Stub() {
	override fun onFilterAdd(j: Long, iArr: IntArray?) {/* Unused */}
	override fun onRoutingChanged(i: Int) {/* Unused */}
	override fun onSetUser(str: String?) {/* Unused */}
	override fun onSyncDone() {/* Unused */}

	override fun onSettingsChanged(output: Output?, outputSettings: OutputSettings?) {
		if (output != null)
			currentOutput = output
		if (outputSettings != null)
			currentSettings = outputSettings

		// Update UI if active
		val ui = MainActivity.getActiveUi()
		ui?.runOnUiThread { ui.updateUi(currentSettings, currentOutput) }
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
