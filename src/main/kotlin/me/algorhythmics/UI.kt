package me.algorhythmics

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

data class UiElements (
	val diracEnabled: Switch,
	val filterEnabled: Switch,
	val sfxEnabled: Switch,
	val eqEnabled: Switch,
	val bands: EqBands,
	val view: ScrollView,
)

// Band frequencies are unknown, the protocol only guarantees 7 float gains
class EqBands(ctx: Context, onBandChange: (index: Int, gain: Float) -> Unit) : LinearLayout(ctx) {
	private val labels = Array(BAND_COUNT) { TextView(ctx) }
	private val sliders = Array(BAND_COUNT) { index ->
		SeekBar(ctx).apply {
			max = gainToProgress(MAX_GAIN)
			progress = gainToProgress(0f)
			setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
				override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
					showGain(index, progressToGain(progress))
					if (fromUser) onBandChange(index, progressToGain(progress))
				}
				override fun onStartTrackingTouch(bar: SeekBar) {}
				override fun onStopTrackingTouch(bar: SeekBar) {}
			})
		}
	}

	init {
		orientation = VERTICAL
		for (i in 0 until BAND_COUNT) {
			showGain(i, 0f)
			addView(labels[i])
			addView(sliders[i])
		}
	}

	fun update(bands: FloatArray) {
		require(bands.size == BAND_COUNT) { "Expected $BAND_COUNT bands, got ${bands.size}" }
		bands.forEachIndexed { i, gain -> sliders[i].progress = gainToProgress(gain) }
	}

	override fun setEnabled(enabled: Boolean) {
		super.setEnabled(enabled)
		sliders.forEach { it.isEnabled = enabled }
	}

	private fun showGain(index: Int, gain: Float) {
		labels[index].text = "Band ${index + 1}: %+.1f dB".format(gain)
	}

	companion object {
		const val BAND_COUNT = 7
		const val MIN_GAIN = -12f
		const val MAX_GAIN = 12f
		const val GAIN_STEP = 0.5f

		private fun gainToProgress(gain: Float) =
			Math.round((gain.coerceIn(MIN_GAIN, MAX_GAIN) - MIN_GAIN) / GAIN_STEP)

		private fun progressToGain(progress: Int) = MIN_GAIN + progress * GAIN_STEP
	}
}

private fun switcher(ctx: MainActivity, name: String, callback: (Boolean) -> Unit): Switch {
	
}

fun toast(msg: String) {
	val ui = MainActivity.getActiveUi()
    ui?.runOnUiThread { Toast.makeText(ui, msg, Toast.LENGTH_LONG).show() }
}

fun toastError(status: Bundle) {
	if (status.containsKey(Keys.EXCEPTION_OCCURRED) && status.getChar(Keys.EXCEPTION_OCCURRED) == 'Y') {
		val clazz = status.getString(Keys.CAUSE_CLASS)
		val message = status.getString(Keys.CAUSE_MESSAGE)
		val msg = "$clazz: $message"
        Log.e("RemoteError", msg)
		toast(msg)
	}
}

fun composeUi(): UiElements {
	val ui = MainActivity.getActiveUi()
		?: throw IllegalStateException("App is not running, this shouldn't be called without a context")

	val view = ScrollView(ui)
	val diracEnabled = switcher(ui, "Dirac HD") { newValue: Boolean -> BOUND. }
	val diracEnabled = switcher(ui, "Dirac HD") { newValue: Boolean -> ctx.onDiracChange(newValue) }
	val diracEnabled = switcher(ui, "Dirac HD") { newValue: Boolean -> ctx.onDiracChange(newValue) }
	val diracEnabled = switcher(ui, "Dirac HD") { newValue: Boolean -> ctx.onDiracChange(newValue) }
	val diracEnabled = switcher(ui, "Dirac HD") { newValue: Boolean -> ctx.onDiracChange(newValue) }

	return view
}
