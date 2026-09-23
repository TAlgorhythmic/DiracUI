package me.algorhythmics

import android.content.Context
import android.view.View
import android.widget.ScrollView
import android.widget.Switch
import android.widget.Toast

data class UiElements(
	val diracEnabled: Switch,
	val filterEnabled: Switch,
	val sfxEnabled: Switch,
	val eqEnabled: Switch,
	val view: ScrollView,
)

private fun switcher(ctx: MainActivity, name: String, callback: (Boolean) -> Unit): Switch {
	
}

fun toast(msg: String) {
	val ui = MainActivity.getActiveUi()
    ui?.runOnUiThread { Toast.makeText(ui, msg, Toast.LENGTH_LONG).show() }
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
