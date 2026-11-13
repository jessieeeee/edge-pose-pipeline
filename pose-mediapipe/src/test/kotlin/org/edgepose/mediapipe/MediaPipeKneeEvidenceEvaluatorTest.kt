package org.edgepose.mediapipe

import org.edgepose.core.KeypointType
import org.edgepose.core.Landmark
import org.edgepose.core.Point2D
import org.edgepose.core.PoseFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaPipeKneeEvidenceEvaluatorTest {
    @Test fun mapsEstimatorFrameIntoSharedKneeEvidence() {
        val evaluator = MediaPipeKneeEvidenceEvaluator { error("bitmap path not used") }
        val evidence = evaluator.evaluateFrame(PoseFrame(0, 0, landmarks()), 4, 8)
        assertEquals(true, evidence?.covered)
        assertEquals(false, evidence?.detected)
    }

    @Test fun emptyEstimatorResultIsUnavailable() {
        val evaluator = MediaPipeKneeEvidenceEvaluator { null }
        assertNull(evaluator.evaluateFrameOrNull(null, 1, 2))
    }

    private fun landmarks() = listOf(
        Landmark(KeypointType.LEFT_HIP, Point2D(.35f, .3f), .9f),
        Landmark(KeypointType.LEFT_KNEE, Point2D(.35f, .55f), .9f),
        Landmark(KeypointType.LEFT_ANKLE, Point2D(.35f, .8f), .9f),
        Landmark(KeypointType.RIGHT_HIP, Point2D(.65f, .3f), .9f),
        Landmark(KeypointType.RIGHT_KNEE, Point2D(.65f, .55f), .9f),
        Landmark(KeypointType.RIGHT_ANKLE, Point2D(.65f, .8f), .9f),
    ).associateBy { it.type }
}
