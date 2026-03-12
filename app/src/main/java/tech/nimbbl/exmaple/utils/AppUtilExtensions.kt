package tech.nimbbl.exmaple.utils


/**
 * Utility extensions for the Nimbbl sample app
 */
object AppUtilExtensions {
    
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

} 