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
            url = uri("https://gitlab.com/api/v4/projects/25847308/packages/maven")
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "Nimbbl-Android-Sample-App"
include(":app")


// Using fat AAR that includes both WebView SDK and Core API SDK


