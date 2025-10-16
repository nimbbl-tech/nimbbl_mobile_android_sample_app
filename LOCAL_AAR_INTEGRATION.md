# Local AAR Integration

Simple integration guide for Nimbbl WebView SDK using local AAR file.

## Setup

### 1. Add AAR File
Copy `nimbbl_mobile_kit_android_webview_sdk-fat-release.aar` to `app/libs/`

### 2. Update build.gradle
```kotlin
dependencies {
    implementation(files("libs/nimbbl_mobile_kit_android_webview_sdk-fat-release.aar"))
}
```

## Requirements
- Android API 21+
- Kotlin 1.9.0+
