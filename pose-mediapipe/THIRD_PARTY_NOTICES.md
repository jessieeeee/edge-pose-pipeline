# Third-Party Notices

## MediaPipe Tasks Vision

The optional `pose-mediapipe` module depends on Google MediaPipe Tasks Vision `0.10.35` (`com.google.mediapipe:tasks-vision`). Review the license distributed with that dependency and its upstream terms before redistributing a binary that includes this module.

The demo includes the Pose Landmarker Full task asset at
`src/main/assets/pose_landmarker_full.task`. It was obtained from Google's
official MediaPipe model distribution and is documented by SHA-256 in
`MODEL_ASSET.md`. Before publishing a derivative binary, review the current
MediaPipe repository license and model distribution terms:

- https://github.com/google-ai-edge/mediapipe/blob/master/LICENSE
- https://ai.google.dev/edge/mediapipe/solutions/vision/pose_landmarker

No camera frames or diagnostic traces are distributed by this repository.
