package me.algorhythmics

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import se.dirac.acs.api.IAudioControlService

private const val TAG: String = "Client"
private val INTENT: Intent = Intent().setClassName("se.dirac.acs", "se.dirac.acs.AudioControlService")

@Volatile
var STARTED: Boolean = false
@Volatile
var BOUND: IAudioControlService? = null

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

fun startService(ctx: Context): Boolean {
	try {
		Log.i(TAG, "Attempting to start DiracAudioControlService...")
	    val res = ctx.startService(INTENT)

		// Service doesn't exist, so bad install
		if (res == null) {
			Log.e(TAG, "Failed to start service, probably doesn't exist")
			return false
		}
	} catch (e: Exception) {
        Log.e(TAG, "Failed to start service", e)
		return false
	}

	Log.i(TAG, "Service started")
	STARTED = true
	return true
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
	return BOUND != null && (BOUND as IBinder).isBinderAlive
}
