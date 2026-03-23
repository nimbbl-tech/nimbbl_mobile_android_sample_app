package tech.nimbbl.exmaple.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * Utility class for common UI operations
 */
object UiUtils {

    /**
     * Show a short toast message
     */
    fun showToast(context: Context, message: String) {
        Log.d("UiUtils", "showToast: $message")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
} 