plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.edgepose.mediapipe"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig { minSdk = 26 }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":pose-core"))
    implementation(libs.mediapipe.tasks.vision)
    testImplementation(libs.junit)
}
