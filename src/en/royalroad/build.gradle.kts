plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "eu.kanade.tachiyomi.extension.en.royalroad"
    compileSdk = 34

    defaultConfig {
        // Extensions are built as standalone installable apps (this is
        // why they ship as .apk files) - applicationId is the package
        // that gets installed on the device.
        applicationId = "eu.kanade.tachiyomi.extension.en.royalroad"
        minSdk = 21
        targetSdk = 34
        // Bump versionCode/versionName together whenever RoyalRoad.kt
        // changes - the index generator reads these back out of this
        // file for index.min.json.
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    compileOnly(project(":extensions-lib-stub"))
    implementation("org.jsoup:jsoup:1.17.2")
}
