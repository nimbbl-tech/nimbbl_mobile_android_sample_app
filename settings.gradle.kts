pluginManagement {

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://jitpack.io")
        }
    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "Nimbbl-Android-Sample-App"
include(":app")

// Local SDK modules (comment out when using Maven dependency only)
//val parentDir = file(settingsDir.parent)
//include(":nimbbl_mobile_kit_core_api_sdk")
//project(":nimbbl_mobile_kit_core_api_sdk").projectDir = File(parentDir, "nimbbl_mobile_kit_core_api_sdk")
//include(":nimbbl_mobile_kit_android_webview_sdk")
//project(":nimbbl_mobile_kit_android_webview_sdk").projectDir = File(parentDir, "nimbbl_mobile_kit_android_webview_sdk")
