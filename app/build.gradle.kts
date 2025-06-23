plugins {
    id("com.android.application") version "8.10.1"
    id("org.jetbrains.kotlin.android") version "1.9.0"
}

android {
    namespace = "tech.nimbbl.exmaple"
    compileSdk = 34

    defaultConfig {
        applicationId = "tech.nimbbl.exmaple"
        minSdk = 24
        targetSdk = 34
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

    // Uncomment below lines if using local projects
     implementation(project(":nimbbl_mobile_kit_android_webview_sdk"))
     implementation(project(":nimbbl_mobile_kit_core_api_sdk"))


    // Optional SDK integrations
    // implementation("tech.nimbbl.sdk:nimbbl-checkout-sdk:3.0.5")
    // implementation("tech.nimbbl.sdk:nimbbl-checkout-core-sdk-java:3.0.5")
    implementation("com.airbnb.android:lottie:3.4.1")
    implementation("com.github.bumptech.glide:glide:4.13.2")
    //implementation("com.github.nimbbl-tech:nimbbl_mobile_kit_core_api_sdk:3.0.7")
    //implementation("com.github.nimbbl-tech:nimbbl_mobile_kit_android_webview_sdk:3.0.7")
}

