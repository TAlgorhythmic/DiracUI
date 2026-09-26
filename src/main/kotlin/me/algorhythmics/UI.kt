package me.algorhythmics

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import se.dirac.acs.api.Device
import se.dirac.acs.api.UsecaseItem

data class UiElements (
    val diracEnabled: Switch,
    val filterEnabled: Switch,
    val sfxEnabled: Switch,
    val eqEnabled: Switch,
    val bands: EqBands,
    val stereoWidth: SeekBar,
    val tonalBalance: SeekBar,
    val loudness: SeekBar,
    val device: Selector<Device>,
	val usecase: Selector<UsecaseItem>,
    val view: ScrollView,
)

// Band frequencies are unknown, the protocol only guarantees a list of float gains
class EqBands(ctx: Context, bandCount: Int, private val onBandChange: (index: Int, gain: Float) -> Unit) : LinearLayout(ctx) {
	private var labels = emptyArray<TextView>()
	private var sliders = emptyArray<SeekBar>()
	val bandCount get() = sliders.size

	init {
		orientation = VERTICAL
		rebuild(bandCount)
	}

	fun update(bands: FloatArray) {
		if (bands.size != bandCount) rebuild(bands.size)
		bands.forEachIndexed { i, gain -> sliders[i].progress = gainToProgress(gain) }
	}

	private fun rebuild(count: Int) {
		removeAllViews()
		labels = Array(count) { TextView(context) }
		sliders = Array(count) { createSlider(it) }
		for (i in 0 until count) {
			showGain(i, 0f)
			addView(labels[i])
			addView(sliders[i])
		}
	}

	private fun createSlider(index: Int) = SeekBar(context).apply {
		max = gainToProgress(MAX_GAIN)
		progress = gainToProgress(0f)
		isEnabled = this@EqBands.isEnabled
		setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
				showGain(index, progressToGain(progress))
				if (fromUser) onBandChange(index, progressToGain(progress))
			}
			override fun onStartTrackingTouch(bar: SeekBar) {}
			override fun onStopTrackingTouch(bar: SeekBar) {}
		})
	}

	override fun setEnabled(enabled: Boolean) {
		super.setEnabled(enabled)
		sliders.forEach { it.isEnabled = enabled }
	}

	private fun showGain(index: Int, gain: Float) {
		labels[index].text = "Band ${index + 1}: %+.1f dB".format(gain)
	}

	companion object {
		const val MIN_GAIN = -12f
		const val MAX_GAIN = 12f
		const val GAIN_STEP = 0.5f

		private fun gainToProgress(gain: Float) =
			Math.round((gain.coerceIn(MIN_GAIN, MAX_GAIN) - MIN_GAIN) / GAIN_STEP)

		private fun progressToGain(progress: Int) = MIN_GAIN + progress * GAIN_STEP
	}
}

class Selector<T>(
	ctx: Context,
	private val label: (T) -> String,
	private val onSelected: (T) -> Unit,
) : Spinner(ctx) {
	private var items = emptyList<T>()
	var selected: T? = null
		private set

	init {
		onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
				val item = items[position]
				// Spinner also fires for programmatic selections, so skip the already selected item
				if (item == selected) return
				selected = item
				onSelected(item)
			}
			override fun onNothingSelected(parent: AdapterView<*>) {}
		}
	}

	fun update(items: List<T>, selected: T? = this.selected) {
		this.items = items
		adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items.map(label)).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
		val index = items.indexOf(selected).coerceAtLeast(0)
		this.selected = items.getOrNull(index)
		setSelection(index)
	}
}

var updating: Boolean = false

private fun switcher(ctx: MainActivity, name: String, callback: (Boolean) -> Unit): Switch {
	return Switch(ctx).apply {
		showText = false
		layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
		)
		text = name
		setOnCheckedChangeListener {_: CompoundButton, newVal: Boolean ->
			if (!updating) {
				isEnabled = false
				callback(newVal)
			}
		}
	}
}

private fun seekBar(ctx: MainActivity, callback: (Float) -> Unit): SeekBar {
	return SeekBar(ctx).apply {
		max = 50
		progress = 25
		setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, newProgress: Int, fromUser: Boolean) {
				if (!fromUser) return
				callback(newProgress / 25f - 1f)
            }

            override fun onStartTrackingTouch(p0: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(p0: android.widget.SeekBar?) {}
        })
	}
}

fun toast(msg: String) {
	MainActivity.getActiveUi()?.apply { runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_LONG).show() } }
}

fun toastError(status: Bundle) {
	if (status.containsKey(Keys.EXCEPTION_OCCURRED) && status.getChar(Keys.EXCEPTION_OCCURRED) == 'Y') {
		val clazz = status.getString(Keys.CAUSE_CLASS)
		val message = status.getString(Keys.CAUSE_MESSAGE)
		val msg = "$clazz: $message"
		Log.e("ServiceError", msg)
		toast(msg)
	}
}

fun composeUi(): UiElements {
	val ui = MainActivity.getActiveUi()
		?: throw IllegalStateException("App is not running, this shouldn't be called without a context")

	// Switches
	val	diracEnabled = switcher(ui, "Dirac HD") {newValue: Boolean ->
		currentSettings.enabled = newValue
		updateSettings(currentSettings)
	}
	val device = Selector(ui, { dev: Device -> dev.name }) {newDevice: Device ->
		currentSettings.device = newDevice
		if (newDevice.id >= 0 && newDevice.filters.isNotEmpty())
			currentSettings.filter = newDevice.filters[0]
		updateSettings(currentSettings)
	}
	val usecase = Selector(ui, { uc: UsecaseItem -> uc.name }) {newUsecase: UsecaseItem ->
		currentSettings.filter.usecase = newUsecase
		updateSettings(currentSettings)
	}
	val filterEnabled = switcher(ui, "Enable Filter") {newValue: Boolean ->
		currentSettings.filterEnabled = newValue
		updateSettings(currentSettings)
	}
	val sfxEnabled = switcher(ui, "Enable SFX") {newValue: Boolean ->
		currentSettings.sfxEnabled = newValue
		updateSettings(currentSettings)
	}
	val eqEnabled = switcher(ui, "Enable Equalizer") { newValue: Boolean ->
		currentSettings.eqEnabled = newValue
		updateSettings(currentSettings)
	}
	val equalizer = EqBands(ui, currentSettings.eqBands.size) { i: Int, gain: Float ->
		currentSettings.eqBands[i] = gain
		updateSettings(currentSettings)
	}
	val stereoWidthTitle = TextView(ui).apply { text = "Stereo Width" }
	val stereoWidth = seekBar(ui) { newValue: Float ->
		currentSettings.stereoWidth = newValue
		updateSettings(currentSettings)
	}
	val tonalBalanceTitle = TextView(ui).apply { text = "Tonal Balance" }
	val tonalBalance = seekBar(ui) { newValue: Float ->
		currentSettings.tonalBalance = newValue
		updateSettings(currentSettings)
	}
	val loudnessTitle = TextView(ui).apply { text = "Loudness" }
	val loudness = seekBar(ui) { newValue: Float ->
		currentSettings.loudness = newValue
		updateSettings(currentSettings)
	}

	val content = LinearLayout(ui).apply { orientation = LinearLayout.VERTICAL }

	// Append all components
	content.addView(diracEnabled)
	content.addView(device)
	content.addView(usecase)
	content.addView(filterEnabled)
	content.addView(sfxEnabled)
	content.addView(eqEnabled)
	content.addView(equalizer)
	content.addView(stereoWidthTitle)
	content.addView(stereoWidth)
	content.addView(tonalBalanceTitle)
	content.addView(tonalBalance)
	content.addView(loudnessTitle)
	content.addView(loudness)

	val view = ScrollView(ui).apply { addView(content) }

	return UiElements(
		diracEnabled,
		filterEnabled,
		sfxEnabled,
		eqEnabled,
		equalizer,
		stereoWidth,
		tonalBalance,
		loudness,
		device,
		usecase,
		view
	)
}
