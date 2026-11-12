package org.edgepose.demo

import org.edgepose.core.KeypointType
import org.edgepose.core.Landmark
import org.edgepose.core.Point2D
import org.edgepose.core.PoseFrame
import org.edgepose.core.TrackingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SquatObservationMapperTest {
    @Test fun mapsRawUnilateralGeometryWithoutMixingMissingSides() {
        val points = listOf(
            landmark(KeypointType.LEFT_HIP, .40f, .20f),
            landmark(KeypointType.LEFT_KNEE, .45f, .50f),
            landmark(KeypointType.LEFT_ANKLE, .35f, .80f),
            landmark(KeypointType.RIGHT_HIP, .60f, .20f),
            landmark(KeypointType.RIGHT_KNEE, .65f, .50f),
            landmark(KeypointType.RIGHT_ANKLE, .65f, .80f),
        )
        val observation = SquatObservationMapper.map(PoseFrame(0, 0, points.associateBy { it.type }), TrackingState.TRACKING)!!
        assertTrue(observation.kneeAlignment!!.covered)
        assertTrue(observation.kneeAlignment!!.left!!.detected)
        assertFalse(observation.kneeAlignment!!.right!!.detected)
        val missing = points.filterNot { it.type == KeypointType.RIGHT_ANKLE }
        val partial = SquatObservationMapper.map(PoseFrame(1, 1, missing.associateBy { it.type }), TrackingState.TRACKING)!!
        assertEquals(null, partial.kneeAlignment!!.right)
        assertFalse(partial.kneeAlignment!!.covered)
    }

    private fun landmark(type: KeypointType, x: Float, y: Float) = Landmark(type, Point2D(x, y), .9f)
}
