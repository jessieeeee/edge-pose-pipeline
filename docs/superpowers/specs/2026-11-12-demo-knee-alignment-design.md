# Demo Knee Alignment Design

## Scope

Extend the standalone public Kotlin core and Android demo with cautious, per-repetition possible knee-inward feedback. This is a visual alignment cue, not a diagnosis. Keep the main VexaFit repository and its private coaching/session infrastructure out of the public repository.

## Architecture

`pose-core` owns a pure Kotlin per-side alignment evaluator and attempt-level alert aggregation. The demo maps MoveNet's left and right hip, knee, and ankle landmarks into core observations; no Android types enter the core API. The existing squat counter remains responsible for lifecycle and rep completion. Its output exposes the current possible alignment cue and cumulative alerted repetitions, while the demo displays those values alongside phase and reps.

## Evidence Rules

- Evaluate sides independently from hip, knee, and ankle X coordinates. Reject a side when any required landmark has confidence below 0.30 or is absent; never combine partial evidence from opposite sides.
- Preserve the source horizontal geometry: a knee crossing the hip relative to the ankle is suspicious above 0.015 normalized image width, while same-direction inward drift is suspicious above 0.020. Strong evidence uses 0.080 crossing or 0.050 drift.
- A single suspect frame is insufficient for a user-facing warning. Aggregate credible, temporally sustained evidence during the active squat attempt, with a minimum 80 ms support window. Reset pending evidence on tracking loss or attempt abort. Do not show a warning when coverage is inadequate.
- Count at most one knee-alignment alert for a completed repetition with supported evidence. No alert for an aborted or uncounted attempt. The cumulative alert count cannot exceed repetitions.
- The demo uses cautious copy: `Watch knee alignment`, not a medical or injury claim. A neutral alignment status appears when evidence is insufficient; it must not assert that alignment is good without credible coverage.

## Validation

Unit tests cover normal alignment, unilateral inward movement, stronger evidence, missing/low-confidence landmarks, insufficient duration, aborted attempts, and one alert maximum per repetition. Run core and Android/demo unit tests, build the debug APK, then install on the connected Pixel. Device checks verify startup, active front camera, inference and UI display. A person performing both normal and intentional inward-knee squats is required to confirm real-world classification; a camera pointed away from the person cannot establish that result.
