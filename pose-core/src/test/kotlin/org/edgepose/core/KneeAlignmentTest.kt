package org.edgepose.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KneeAlignmentTest {
    @Test fun crossingAndInwardDriftAreDetectedPerSide() {
        assertTrue(KneeAlignmentEvaluator.evaluate(frame(0.45f)).left!!.detected)
        assertTrue(KneeAlignmentEvaluator.evaluate(frame(0.375f)).left!!.detected)
        assertFalse(KneeAlignmentEvaluator.evaluate(frame(0.34f)).left!!.detected)
    }

    @Test fun missingOrUnreliableGeometryDoesNotBecomeEvidence() {
        assertEquals(null, KneeAlignmentEvaluator.evaluate(frame(0.45f, 0.29f)).left)
        assertEquals(null, KneeAlignmentEvaluator.evaluate(frame(0.45f).copy(landmarks = emptyMap())).left)
    }

    @Test fun strongCrossingRequiresLargerDisplacement() {
        assertFalse(KneeAlignmentEvaluator.evaluate(frame(.45f)).left!!.strongDetected)
        assertTrue(KneeAlignmentEvaluator.evaluate(frame(.49f)).left!!.strongDetected)
    }

    private fun frame(kneeX: Float, confidence: Float = 0.9f): PoseFrame {
        val points = listOf(
            Landmark(KeypointType.LEFT_HIP, Point2D(0.40f, 0.2f), 0.9f),
            Landmark(KeypointType.LEFT_KNEE, Point2D(kneeX, 0.5f), confidence),
            Landmark(KeypointType.LEFT_ANKLE, Point2D(0.35f, 0.8f), 0.9f),
        )
        return PoseFrame(0, 0, points.associateBy { it.type })
    }
}
