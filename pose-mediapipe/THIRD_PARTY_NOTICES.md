# Third-Party Notices

## MediaPipe Tasks Vision

The optional `pose-mediapipe` module depends on Google MediaPipe Tasks Vision `0.10.35` (`com.google.mediapipe:tasks-vision`). Review the license distributed with that dependency and its upstream terms before redistributing a binary that includes this module.

No MediaPipe task model is included. `MediaPipePoseEstimator` expects a compatible Pose Landmarker `.task` asset in the Android asset path supplied to its constructor. Obtain that model from an authorized source and review its specific license before use or redistribution. The VexaFit `pose_landmarker_full.task` binary was not copied.
