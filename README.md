# Edge Pose Pipeline

An independent Android pose-analysis sample with a platform-independent Kotlin core. This checkout is an in-progress local extraction from VexaFit and is not yet a published repository.

## Current status

The pure Kotlin/JVM core, Android adapters, and MoveNet-backed camera demo are implemented locally. `pose-android` contains CameraX YUV-to-RGB preprocessing, MoveNet coordinate/output mapping, and a LiteRT inference wrapper. Optional `pose-mediapipe` contains a MediaPipe Tasks Vision estimator, landmark mapper, and the authorized `pose_landmarker_full.task` asset (SHA-256: `4eaa5eb7a98365221087693fcc286334cf0858e2eb6e15b506aa4a7ecdcec4ad`). The demo has separate synthetic replay and front-camera live inference modes. `demo-app/src/main/assets/movenet_thunder.tflite` is the authorized MoveNet asset; SHA-256: `41641538679ec79b07d4101e591dda47d098c09af29607674b2a40b8a3798dd3`. At runtime the MoveNet estimator validates UINT8 `[1,256,256,3]` input and FLOAT32 `[1,17,3]` output before inference.

## Build

```bash
./gradlew :pose-core:test
```

Android modules are opt-in so the JVM core can be tested without resolving the Android Gradle Plugin:

```bash
./gradlew -PincludeAndroid=true :pose-android:testDebugUnitTest
```

Build and test the model-free replay demo with:

```bash
./gradlew -PincludeDemo=true :demo-app:testDebugUnitTest :demo-app:assembleDebug
```

The MediaPipe adapter is separately opt-in:

```bash
./gradlew -PincludeMediaPipe=true :pose-mediapipe:testDebugUnitTest
```

To package the MediaPipe adapter and task model with the demo APK:

```bash
./gradlew -PincludeAndroid=true -PincludeDemo=true -PincludeDemoMediaPipe=true :demo-app:assembleDebug
```


The core is designed to consume normalized pose observations and produce deterministic analysis results. It does not include accounts, network services, or VexaFit product configuration.
