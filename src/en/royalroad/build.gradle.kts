plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "eu.kanade.tachiyomi.extension.en.royalroad"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        // Bump versionCode/versionName together whenever RoyalRoad.kt
        // changes - the index generator reads these back out of the
        // built APK's manifest for index.min.json.
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
}

dependencies {
    compileOnly("com.github.tachiyomiorg:extensions-lib:1.4")
    implementation("org.jsoup:jsoup:1.17.2")
}
