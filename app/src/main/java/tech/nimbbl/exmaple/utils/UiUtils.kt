package tech.nimbbl.exmaple.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

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

    /**
     * Show a long toast message
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Show a simple alert dialog
     */
    fun showAlert(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String = "OK",
        onPositiveClick: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveClick?.invoke()
            dialog.dismiss()
        }
        builder.show()
    }

    /**
     * Show a confirmation dialog
     */
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String = "OK",
        negativeButtonText: String = "Cancel",
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveClick()
            dialog.dismiss()
        }
        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            onNegativeClick?.invoke()
            dialog.dismiss()
        }
        builder.show()
    }
} 