# MoveNet Model Asset

The demo bundles `demo-app/src/main/assets/movenet_thunder.tflite`, obtained
from the official TensorFlow Hub MoveNet Thunder distribution. Its SHA-256 is
`41641538679ec79b07d4101e591dda47d098c09af29607674b2a40b8a3798dd3`.
Review the upstream TensorFlow Models license and model terms before making a
derivative binary distribution:

- https://tfhub.dev/google/lite-model/movenet/singlepose/thunder/tflite/float16/4
- https://github.com/tensorflow/models/blob/master/LICENSE

Library consumers may provide another model file, but it must satisfy the same contract.

The estimator expects the MoveNet Thunder contract documented in the source model documentation: UINT8 input `[1, 256, 256, 3]` containing packed RGB bytes and FLOAT32 output `[1, 17, 3]` containing `[y, x, score]` rows. The caller owns image conversion, square crop/resize, rotation, mirroring, and model-file lifecycle. Do not add downloaded model files to Git unless the specific artifact's license explicitly permits redistribution.
