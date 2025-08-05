package tech.nimbbl.exmaple.utils

class Constants {
    companion object {
        // Environment URLs for settings screen only
        const val BASE_URL_PROD = "https://api.nimbbl.tech/"
        const val BASE_URL_PRE_PROD = "https://apipp.nimbbl.tech/"
        const val BASE_URL_QA_DEFAULT = "https://qa1api.nimbbl.tech/"
        const val BASE_URL_QA4 = "https://qa4api.nimbbl.tech/"
        
        // Default values for settings
        const val DEFAULT_ENVIRONMENT = BASE_URL_PROD
        const val DEFAULT_QA_URL = BASE_URL_QA4
    }
}