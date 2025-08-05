package tech.nimbbl.exmaple.utils

import android.content.Context
import android.widget.Toast

/**
 * Utility extensions for the Nimbbl sample app
 */
object AppUtilExtensions {
    
    /**
     * Show a toast message
     */
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Format and validate a URL to ensure proper formatting
     * @param url The URL to format
     * @return Properly formatted URL
     */
    fun formatUrl(url: String): String {
        var formattedUrl = url.trim()
        
        // Remove double slashes (except for protocol)
        formattedUrl = formattedUrl.replace("//", "/")
        formattedUrl = formattedUrl.replace("https:/", "https://")
        formattedUrl = formattedUrl.replace("http:/", "http://")
        
        // Ensure URL ends with slash
        if (!formattedUrl.endsWith("/")) {
            formattedUrl += "/"
        }
        
        return formattedUrl
    }
    
    /**
     * Validate if a URL is properly formatted
     * @param url The URL to validate
     * @return True if URL is valid
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            val formattedUrl = formatUrl(url)
            android.util.Patterns.WEB_URL.matcher(formattedUrl).matches()
        } catch (e: Exception) {
            false
        }
    }
} 