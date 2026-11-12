package org.edgepose.core

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingQualityEvaluatorTest {
    @Test
    fun distinguishesMissingPartialAndLowConfidencePose() {
        val evaluator = TrackingQualityEvaluator()

        assertEquals(TrackingState.NO_PERSON, evaluator.evaluate(null))
        assertEquals(TrackingState.PARTIAL_BODY, evaluator.evaluate(frame(listOf(landmark(KeypointType.LEFT_HIP, 0f)))))
        assertEquals(
            TrackingState.LOW_CONFIDENCE,
            evaluator.evaluate(frame(listOf(
                landmark(KeypointType.LEFT_HIP, 0.2f),
                landmark(KeypointType.LEFT_KNEE, 0.3f),
                landmark(KeypointType.LEFT_ANKLE, 0.1f),
            ))),
        )
    }

    private fun frame(points: List<Landmark>) = PoseFrame(
        frameId = 1,
        timestampNanos = 1,
        landmarks = points.associateBy(Landmark::type),
    )

    private fun landmark(type: KeypointType, confidence: Float) =
        Landmark(type, Point2D(0.5f, 0.5f), confidence)
}
