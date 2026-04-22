package tech.nimbbl.exmaple.utils

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import tech.nimbbl.exmaple.R

object SystemBars {
    fun apply(activity: Activity) {
        val window = activity.window

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val isNightMode =
            (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        if (isNightMode) {
            window.statusBarColor = ContextCompat.getColor(activity, R.color.black)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        } else {
            window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        }
    }
}

