plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.0"
}

android {
    namespace = "tech.nimbbl.exmaple"
    compileSdk = 36

    defaultConfig {
        applicationId = "tech.nimbbl.exmaple"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
    
    lint {
        disable += "NotificationPermission"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    api(platform("com.squareup.okhttp3:okhttp-bom:4.8.1"))
    api("com.squareup.okhttp3:okhttp")
    api("com.squareup.okhttp3:logging-interceptor")

    // WebView SDK dependency (includes Core API SDK transitively)
    //implementation(project(":nimbbl_mobile_kit_android_webview_sdk"))
    
    // SDK integrations - Production version
    //implementation("com.github.nimbbl-tech:nimbbl_mobile_kit_android_webview_sdk:3.0.12")
    implementation("org.bitbucket.nimbbl-tech:nimbbl_mobile_kit_android_webview_sdk:v4.0.2")
}

