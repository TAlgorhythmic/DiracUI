package me.algorhythmics

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log

/**
 * Binder host for [IDiracService]. Clients bind with an explicit intent for this
 * class; the stub below is what they talk to.
 *
 * The state lives in the companion rather than on the instance so it survives
 * the service being unbound and destroyed - the receivers keep publishing
 * whether or not anything is bound.
 */
class DiracService : Service() {

    private val callbacks = RemoteCallbackList<IDiracCallback>()

    private val binder = object : IDiracService.Stub() {

        override fun getDeviceType(): Int = currentType

        override fun getDeviceName(): String? = currentName

        override fun registerCallback(cb: IDiracCallback?) {
            if (cb != null) callbacks.register(cb)
        }

        override fun unregisterCallback(cb: IDiracCallback?) {
            if (cb != null) callbacks.unregister(cb)
        }
    }

    override fun onCreate() {
        super.onCreate()

        live = this
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()

        live = null
        callbacks.kill()
    }

    private fun broadcast(type: Int, name: String?) {
        val count = callbacks.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbacks.getBroadcastItem(i).onDeviceChanged(type, name)
                } catch (e: RemoteException) {
                    // The client process is gone; RemoteCallbackList drops it for us.
                    Log.w(TAG, "callback dropped", e)
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }

    companion object {

        private const val TAG = "DiracService"

        @Volatile
        private var live: DiracService? = null

        @Volatile
        private var currentType = IDiracService.DEVICE_NONE

        @Volatile
        private var currentName: String? = null

        /**
         * Record a device change and push it to any bound client. Called from the
         * receivers, which run in this same process; a no-op as far as clients are
         * concerned while nothing is bound.
         */
        fun publish(type: Int, name: String?) {
            currentType = type
            currentName = name

            live?.broadcast(type, name)
        }
    }
}
