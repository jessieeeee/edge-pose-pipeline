package org.edgepose.mediapipe

import org.junit.Test

class MediaPipeRgbFrameEvaluatorTest {
    @Test(expected = IllegalArgumentException::class)
    fun rejectsWrongTensorSize() {
        val evaluator = MediaPipeRgbFrameEvaluator(MediaPipeKneeEvidenceEvaluator { error("not reached") })
        evaluator.evaluate(ByteArray(3), 1, 2)
    }
}
