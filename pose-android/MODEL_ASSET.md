# MoveNet Model Asset

The demo bundles `demo-app/src/main/assets/movenet_thunder.tflite` as the authorized open-source MoveNet Thunder asset. Library consumers may provide another model file, but it must satisfy the same contract.

The estimator expects the MoveNet Thunder contract documented in the source model documentation: UINT8 input `[1, 256, 256, 3]` containing packed RGB bytes and FLOAT32 output `[1, 17, 3]` containing `[y, x, score]` rows. The caller owns image conversion, square crop/resize, rotation, mirroring, and model-file lifecycle. Do not add downloaded model files to Git unless the specific artifact's license explicitly permits redistribution.
