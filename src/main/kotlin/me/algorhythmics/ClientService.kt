package me.algorhythmics

import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.sqlite.SQLiteDatabase
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
var STARTED: Boolean = false
@Volatile
var BOUND: IAudioControlService? = null

// Config state
@Volatile
var currentSettings: OutputSettings = OutputSettings(Device.NOTHING_DEVICE, Filter.INTERNAL_FILTER)
@Volatile
var currentOutput: Output = Output.INTERNAL

private val INTERNAL_USECASES: EnumSet<Usecase> = EnumSet.of(Usecase.INTERNAL_PANORAMA, Usecase.INTERNAL_POWERSOUND)
private val EXTERNAL_USECASES: EnumSet<Usecase> = EnumSet.of(Usecase.EXTERNAL_HEADSET, Usecase.EXTERNAL_MRC)

private fun loadPreset(name: String, internal: Boolean): OutputSettings {
	val db = App.getInstance().database.writableDatabase
	val values = ContentValues().apply { put("name", name) }
	db.insertWithOnConflict("presets", null, values, SQLiteDatabase.CONFLICT_IGNORE)

	return db.query("presets", null, "name=?", arrayOf(name), null, null, null).use { cursor ->
		cursor.moveToFirst()
		OutputSettings(cursor).apply { if (internal) device.id = -1 }
	}
}

fun applyUpdatedSettings() {
	val bound = BOUND ?: return

	val internal = !HeadsetReceiver.PLUGGED && BluetoothReceiver.BLUETOOTH_ACTIVE == null
	val presetName = BluetoothReceiver.BLUETOOTH_ACTIVE
		?: if (HeadsetReceiver.PLUGGED) "headphones" else "internal"

	if (!bound.setOutput(loadPreset(presetName, internal))) {
		Log.w(TAG, "Service rejected preset '$presetName'")
	}
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

		val devices = bind.listDevices("en", Output.EXTERNAL)
		val internalDevices = bind.listDevices("en", Output.INTERNAL)

		for (device in devices) {
            instance.devices[device.id] = device
            for (filter in device.filters) {
                instance.filters[filter.id] = filter
            }
		}
		for (device in internalDevices) {
            instance.devices[device.id] = device
            for (filter in device.filters) {
                instance.filters[filter.id] = filter
				Filter.INTERNAL_FILTER = filter
            }
		}

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
