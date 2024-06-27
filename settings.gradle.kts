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
    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nimbbl-Android-Sample-App"
include(":app")

include(":nimbbl_mobile_kit_android_webview_sdk")
include(":nimbbl_mobile_kit_core_api_sdk")
include(":nimbbl_mobile_kit_android_native_ui_sdk")
