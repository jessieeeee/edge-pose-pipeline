plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.edgepose.android"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = 26
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":pose-core"))
    implementation("androidx.camera:camera-core:1.4.1")
    implementation(libs.litert)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
