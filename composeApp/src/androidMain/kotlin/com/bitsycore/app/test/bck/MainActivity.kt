package com.bitsycore.app.test.bck

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		setContent {
			SystemBarModeController.setLightMode(this)
			val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				dynamicLightColorScheme(this)
			} else {
				MaterialTheme.colorScheme
			}
            MaterialTheme(colorScheme = colorScheme) {
                App()
            }
		}
	}
}