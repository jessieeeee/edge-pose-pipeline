plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.edgepose.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.edgepose.demo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":pose-core"))
    implementation(project(":pose-android"))
    implementation(project(":pose-mediapipe"))
    implementation("androidx.activity:activity:1.10.0")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.google.guava:guava:27.0.1-android")
    testImplementation(libs.junit)
}
