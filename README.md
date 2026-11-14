# Edge Pose Pipeline

An on-device Android squat analysis demo built around a model-agnostic Kotlin core. It combines MoveNet for the primary real-time pose stream with MediaPipe Pose Landmarker for sampled secondary knee-alignment evidence.

The repository is interview-oriented: deterministic judgment logic is isolated in a JVM-testable core, Android camera and inference code lives in adapters, and the demo renders core results without duplicating exercise logic.

## What It Demonstrates

- CameraX front-camera capture with latest-frame backpressure.
- MoveNet Thunder inference with `UINT8 [1,256,256,3]` input and `FLOAT32 [1,17,3]` output validation.
- MediaPipe Pose Landmarker sampled asynchronously during an active squat.
- Per-side knee-alignment geometry and conservative evidence merging.
- Squat phase detection, repetition counting, tracking-quality gating, and sustained knee-risk aggregation.
- Bounded secondary scheduling: latest-request replacement, six samples per attempt, 400 ms minimum spacing, and a two-second deadline.
- Synthetic replay mode for deterministic inspection without a camera.
- Local JSONL diagnostics without storing camera frames.

## Architecture

```text
CameraX ImageProxy
        |
        v
YUV -> RGB 256x256 preprocessing
        |
        +--> MoveNet --------------------+
        |                                |
        |                         PoseFrame + tracking
        |                                |
        |                         SquatAttemptCounter
        |                                |
        +--> MediaPipe (sampled)          |
                 |                       |
                 +--> secondary knee ----+--> KneeEvidenceMerger
                                             |
                                             v
                                      AnalysisResult -> demo UI
```

| Module | Responsibility |
| --- | --- |
| `pose-core` | Pure Kotlin pose contracts, geometry, tracking, squat phases, counting, knee evidence, scheduling, and trace encoding. |
| `pose-android` | Camera preprocessing, MoveNet LiteRT adapter, coordinate mapping, and frame admission. |
| `pose-mediapipe` | MediaPipe Tasks Vision adapter, landmark mapping, RGB bridge, and model asset. |
| `demo-app` | Camera permission, preview, overlay, metrics, replay mode, and local diagnostics. |

The core has no Android, CameraX, MediaPipe, network, account, or product dependencies.

## Why Two Models?

MoveNet is the primary stream because it runs on every camera frame and drives tracking, squat phase, and repetition counting. MediaPipe is not a second counter: it runs only during an active squat attempt and contributes additional knee evidence. Evidence is merged per leg, and a delayed or failed MediaPipe evaluation cannot block the MoveNet loop.

## Build And Test

Requirements: JDK 17, Android SDK/AGP dependencies available locally, and an Android device or emulator for the camera demo.

Pure Kotlin tests:

```bash
./gradlew :pose-core:test --offline --no-daemon
```

Complete validation and debug APK:

```bash
./gradlew \
  -PincludeAndroid=true \
  -PincludeDemo=true \
  :pose-core:test \
  :pose-mediapipe:test \
  :pose-android:testDebugUnitTest \
  :demo-app:testDebugUnitTest \
  :demo-app:assembleDebug \
  --offline --no-daemon
```

APK output: `demo-app/build/outputs/apk/debug/demo-app-debug.apk`.

```bash
adb install -r demo-app/build/outputs/apk/debug/demo-app-debug.apk
adb shell am start -n org.edgepose.demo/.MainActivity
```

Grant camera permission, switch from replay to camera mode, and keep the full body and both feet inside the preview.

## Diagnostic Trace

Camera mode writes versioned JSONL events to the app-private `files/pose-diagnostic-traces/` directory. A trace correlates normalized MoveNet landmarks and confidence, tracking/phase/repetition state, primary and merged knee evidence, MediaPipe scheduling decisions, results, and errors.

Camera images, RGB buffers, and bitmaps are never written. Each trace is capped at 5 MiB and the directory retains at most five recent sessions.

```bash
adb shell run-as org.edgepose.demo sh -c \
  'ls -t files/pose-diagnostic-traces/*.jsonl | head -1 | xargs cat' \
  > pose-trace.jsonl
```

Traces contain pose coordinates and should be reviewed and anonymized before sharing.

## Model Assets And Licensing

- MoveNet Thunder: `demo-app/src/main/assets/movenet_thunder.tflite` (SHA-256: `41641538679ec79b07d4101e591dda47d098c09af29607674b2a40b8a3798dd3`).
- MediaPipe Pose Landmarker Full: `pose-mediapipe/src/main/assets/pose_landmarker_full.task` (SHA-256: `4eaa5eb7a98365221087693fcc286334cf0858e2eb6e15b506aa4a7ecdcec4ad`).

See [`pose-android/MODEL_ASSET.md`](pose-android/MODEL_ASSET.md), [`pose-mediapipe/MODEL_ASSET.md`](pose-mediapipe/MODEL_ASSET.md), and [`pose-mediapipe/THIRD_PARTY_NOTICES.md`](pose-mediapipe/THIRD_PARTY_NOTICES.md) for provenance and upstream terms. Source code is MIT; third-party model and dependency terms remain applicable.

## Scope And Limitations

This is a standalone technical demo with no backend, authentication, training history, raw-video upload, medical diagnosis, or production coaching workflow. Knee alignment is a conservative visual-form signal, not a clinical assessment. Accuracy depends on camera placement, lighting, full-body visibility, and model confidence.

## Resume-Ready Summary

> Built an on-device Android squat-analysis pipeline with a pure Kotlin core, CameraX preprocessing, MoveNet primary inference, and asynchronously sampled MediaPipe knee evidence. Isolated deterministic phase/counting logic from Android adapters, added bounded secondary scheduling and per-side evidence fusion, and implemented privacy-preserving JSONL diagnostics for frame-level debugging without storing camera video.

## License

Source code: MIT. See [`LICENSE`](LICENSE). Third-party dependencies and model assets are subject to their respective notices and upstream terms.
