pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EdgePosePipeline"
include(":pose-core")
if (providers.gradleProperty("includeAndroid").orNull == "true") {
    include(":pose-android")
}
if (providers.gradleProperty("includeMediaPipe").orNull == "true" ||
    providers.gradleProperty("includeDemoMediaPipe").orNull == "true" ||
    providers.gradleProperty("includeDemo").orNull == "true"
) {
    include(":pose-mediapipe")
}
if (providers.gradleProperty("includeDemo").orNull == "true") {
    include(":demo-app")
}
