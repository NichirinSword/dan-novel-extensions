// Root build file. Individual sources (under src/<lang>/<name>/) apply
// these plugins themselves; declaring versions once here keeps every
// source module in sync instead of drifting independently.

plugins {
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
