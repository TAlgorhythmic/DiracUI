package me.algorhythmics

import android.content.Context
import android.view.View
import android.widget.ScrollView
import android.widget.Switch

data class UiElements(
	val diracEnabled: Switch,
	val filterEnabled: Switch,
	val sfxEnabled: Switch,
	val eqEnabled: Switch,
	val view: ScrollView,
)

private fun switcher(ctx: MainActivity, name: String, callback: (Boolean) -> Unit): Switch {
	
}

fun toast() {
	if (MainActivity. != null)
}

fun composeUi(): UiElements {
	val view = ScrollView(ctx)
	val diracEnabled = switcher(ctx, "Dirac HD") { newValue: Boolean -> BOUND. }
	val diracEnabled = switcher(ctx, "Dirac HD") { newValue: Boolean -> ctx.onDiracChange(newValue) }
	val diracEnabled = switcher(ctx, "Dirac HD") { newValue: Boolean -> ctx.onDiracChange(newValue) }
	val diracEnabled = switcher(ctx, "Dirac HD") { newValue: Boolean -> ctx.onDiracChange(newValue) }
	val diracEnabled = switcher(ctx, "Dirac HD") { newValue: Boolean -> ctx.onDiracChange(newValue) }

	return view
}
