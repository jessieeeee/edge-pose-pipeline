# Demo Knee Alignment Implementation Plan

> **For agentic workers:** Execute these checked steps in the current session. Do not create another repository or push a remote.

**Goal:** Show cautious live knee-alignment feedback and count at most one supported alignment alert per completed squat.

**Architecture:** Pure Kotlin geometry evaluates each complete leg independently. The attempt counter accumulates sustained evidence and publishes per-rep totals. The Android demo supplies raw MoveNet landmarks and renders the resulting metrics.

**Tech Stack:** Kotlin/JVM, JUnit 4, CameraX, MoveNet, Android Gradle.

---

### Task 1: Per-side geometry

**Files:** Create `pose-core/src/main/kotlin/org/edgepose/core/KneeAlignment.kt`; create `pose-core/src/test/kotlin/org/edgepose/core/KneeAlignmentTest.kt`.

- [ ] Add a JUnit test using complete `PoseFrame` landmarks: ankle X 0.35, hip X 0.40, knee X 0.45 must be detected as crossing; knee X 0.375 must be detected as same-direction inward drift; knee X 0.34 must be clear. One side missing or below 0.30 must produce no side evidence. Run `./gradlew :pose-core:test --offline` and verify red.
- [ ] Implement `KneeAlignmentEvaluator.evaluate(frame: PoseFrame): KneeAlignmentEvidence` with per-side outcomes and `covered` status. Use absolute X distances: crossing when ankle-to-hip and knee-to-hip have opposite signs and knee-to-hip > 0.015; same-direction inward drift when ankle-to-hip magnitude minus knee-to-hip magnitude > 0.020. Strong limits are 0.080 and 0.050. Return uncovered for absent/low-confidence geometry. Run core tests and verify green.

### Task 2: Attempt aggregation

**Files:** Modify `pose-core/src/main/kotlin/org/edgepose/core/SquatPhaseEngine.kt`, `SquatAttemptCounter.kt`, and `SquatAttemptCounterTest.kt`.

- [ ] Write red tests that supply alignment evidence on successive attempt frames. A credible risk lasting at least 80 ms should show a warning and add one alert on completion; a shorter burst, tracking loss, or aborted attempt should not count. A second risk on the same repetition must not double-count. Assert `alertCount <= repetitionCount`.
- [ ] Add optional evidence to `SquatObservation` and optional alignment fields to `AnalysisResult` with defaults, preserving old callers. During active attempts accumulate only same-side consecutive credible risk durations; reset pending evidence on non-risk/uncovered or abort. Commit one alert when a rep completes. Run all core tests.

### Task 3: Android demo integration

**Files:** Modify `demo-app/src/main/kotlin/org/edgepose/demo/MainActivity.kt`, `demo-app/src/main/res/values/strings.xml`; create `demo-app/src/test/kotlin/org/edgepose/demo/KneeAlignmentIntegrationTest.kt` if mapping is extracted to a testable helper.

- [ ] Write a red test asserting that raw landmark mapping passes both complete legs to the evaluator and a low-confidence landmark leaves its side uncovered. Keep preview mirroring separate from judgment geometry.
- [ ] Evaluate the raw `PoseFrame` once per analysis frame, pass its evidence into the squat observation and display `Watch knee alignment` only for supported active evidence; otherwise show `Alignment unavailable` for uncovered input or `No alignment alert` for covered input. Display cumulative `Knee alerts N`. Verify demo tests.

### Task 4: Verification

- [ ] Run `./gradlew -PincludeAndroid=true -PincludeDemo=true :pose-core:test :pose-android:testDebugUnitTest :demo-app:testDebugUnitTest :demo-app:assembleDebug --no-daemon --offline` and `git diff --check`.
- [ ] Install debug APK through `adb -s 192.168.0.104:5555 install -r demo-app/build/outputs/apk/debug/demo-app-debug.apk`; launch camera mode and inspect UI/logs. Do not claim real-world knee classification without a person performing representative normal and intentional inward-knee squats.
- [ ] Commit the focused implementation in the standalone local repository; do not create or publish a GitHub remote.
