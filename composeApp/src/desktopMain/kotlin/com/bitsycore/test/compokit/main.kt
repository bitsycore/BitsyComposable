package com.bitsycore.test.compokit

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
	Window(
		onCloseRequest = ::exitApplication,
		state = rememberWindowState(
			size = DpSize(720.dp, 960.dp)
		),
		title = "CompoKit - Demo App",
	) {
		App()
	}
}