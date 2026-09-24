package me.algorhythmics

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import se.dirac.acs.api.Device
import se.dirac.acs.api.Filter
import se.dirac.acs.api.IAudioControlService
import se.dirac.acs.api.IAudioControlServiceCallback
import se.dirac.acs.api.Output
import se.dirac.acs.api.OutputSettings

private const val TAG: String = "Client"
private val INTENT: Intent = Intent().setClassName("se.dirac.acs", "se.dirac.acs.AudioControlService")

@Volatile
var STARTED: Boolean = false
@Volatile
var BOUND: IAudioControlService? = null

// Config state
@Volatile
var currentSettings: OutputSettings = OutputSettings(Device.NOTHING_DEVICE, Filter.NOTHING_FILTER)
@Volatile
var currentOutput: Output = Output.INTERNAL

private val CONNECTION = object: ServiceConnection {
    override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
        BOUND = IAudioControlService.Stub.asInterface(binder)
		Log.i(TAG, "Service bound!")
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
