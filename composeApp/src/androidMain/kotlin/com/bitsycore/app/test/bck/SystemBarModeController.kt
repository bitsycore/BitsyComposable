package com.bitsycore.app.test.bck

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import java.lang.reflect.Field
import java.lang.reflect.Method

object SystemBarModeController {

	fun setLightMode(activity: Activity) {
		val window = activity.window
		setStatusBarLightMode(window, true)
		setNavigationBarLightMode(window, true)
	}

	fun setDarkMode(activity: Activity) {
		val window = activity.window
		setStatusBarLightMode(window, false)
		setNavigationBarLightMode(window, false)
	}

	private fun setStatusBarLightMode(window: Window, light: Boolean) {
		if (Build.VERSION.SDK_INT >= 30) {
			window.insetsController?.setSystemBarsAppearance(
				if (light) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
				WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
			)
		} else {
			val decorView = window.decorView
			var flags = decorView.systemUiVisibility
			flags = if (light) {
				flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
			} else {
				flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
			}
			decorView.systemUiVisibility = flags
		}
		setMIUIStatusBarDarkIcon(window, light)
		setFlymeStatusBarDarkIcon(window, light)
	}

	private fun setNavigationBarLightMode(window: Window, light: Boolean) {
		if (Build.VERSION.SDK_INT >= 30) {
			window.insetsController?.setSystemBarsAppearance(
				if (light) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
				WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
			)
		} else if (Build.VERSION.SDK_INT >= 26) {
			val decorView = window.decorView
			var flags = decorView.systemUiVisibility
			flags = if (light) {
				flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
			} else {
				flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
			}
			decorView.systemUiVisibility = flags
		}
	}

	private fun setMIUIStatusBarDarkIcon(window: Window, dark: Boolean) {
		try {
			val clazz: Class<out Window> = window.javaClass

			@SuppressLint("PrivateApi")
			val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
			val field: Field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
			val darkModeFlag: Int = field.getInt(layoutParams)
			val extraFlagField: Method = clazz.getMethod(
				"setExtraFlags",
				Int::class.javaPrimitiveType,
				Int::class.javaPrimitiveType
			)
			extraFlagField.invoke(window, if (dark) darkModeFlag else 0, darkModeFlag)
		} catch (_: Exception) {
		}
	}

	private fun setFlymeStatusBarDarkIcon(window: Window, dark: Boolean) {
		try {
			val lp = window.attributes
			val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
			val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
			darkFlag.isAccessible = true
			meizuFlags.isAccessible = true
			val bit = darkFlag.getInt(null)
			var value = meizuFlags.getInt(lp)
			value = if (dark) value or bit else value and bit.inv()
			meizuFlags.setInt(lp, value)
			window.attributes = lp
		} catch (_: Exception) {
		}
	}
}