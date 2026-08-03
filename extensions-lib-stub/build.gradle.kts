plugins {
    id("org.jetbrains.kotlin.jvm")
}

// This module exists purely so the extension modules have something to
// compile against locally (`compileOnly project(":extensions-lib-stub")`)
// instead of relying on the now-unreliable external tachiyomiorg
// extensions-lib artifact. Because it's referenced with `compileOnly`,
// none of this code is ever packaged into the built extension APKs -
// at runtime, Dantotsu supplies its own real implementations of these
// same classes from its own classpath. The method bodies here never
// actually run; only the method *signatures* need to match.

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jsoup:jsoup:1.17.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
