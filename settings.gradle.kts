rootProject.name = "dantotsu-novel-extensions"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

// Auto-discovers every source module: any directory two levels under
// src/ (src/<lang>/<name>/) that contains its own build.gradle.kts gets
// registered as a Gradle module automatically. Adding a new source is
// then just "add a folder" - no manual settings.gradle.kts editing.
val srcDir = file("src")
if (srcDir.exists()) {
    srcDir.listFiles { f -> f.isDirectory }?.forEach { langDir ->
        langDir.listFiles { f -> f.isDirectory }?.forEach { sourceDir ->
            val buildFile = File(sourceDir, "build.gradle.kts")
            if (buildFile.exists()) {
                val path = ":src:${langDir.name}:${sourceDir.name}"
                include(path)
                project(path).projectDir = sourceDir
            }
        }
    }
}
