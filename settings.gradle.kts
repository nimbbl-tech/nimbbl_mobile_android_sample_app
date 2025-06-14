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
            url = uri("https://gitlab.com/api/v4/projects/25847308/packages/maven")
        }
        maven { url = uri("https://jitpack.io") }
    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://gitlab.com/api/v4/projects/25847308/packages/maven")
        }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Nimbbl-Android-Sample-App"
include(":app")

/*include(":nimbbl_mobile_kit_android_webview_sdk")
include(":nimbbl_mobile_kit_core_api_sdk")*/
