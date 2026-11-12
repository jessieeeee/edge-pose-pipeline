package org.edgepose.android

import androidx.camera.core.ImageProxy
import org.edgepose.core.LatestFrameGate

/** CameraX ownership adapter for the platform-independent latest-frame gate. */
class FrameAdmissionController {
    private val gate = LatestFrameGate<ImageProxy>()

    fun tryAcquire(image: ImageProxy): LatestFrameGate.Lease<ImageProxy>? = gate.tryAcquire(image)
}
