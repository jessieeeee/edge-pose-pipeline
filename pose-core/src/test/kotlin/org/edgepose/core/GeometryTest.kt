package org.edgepose.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class GeometryTest {
    @Test
    fun calculatesRightAngleAtMiddlePoint() {
        val angle = Geometry.angleDegrees(
            first = Point2D(1f, 0f),
            vertex = Point2D(0f, 0f),
            last = Point2D(0f, 1f),
        )

        assertEquals(90f, angle!!, 0.001f)
    }

    @Test
    fun returnsNullForDegenerateSegment() {
        val angle = Geometry.angleDegrees(
            first = Point2D(0f, 0f),
            vertex = Point2D(0f, 0f),
            last = Point2D(1f, 0f),
        )

        assertNull(angle)
    }

    @Test
    fun rejectsUnnormalizedConfidence() {
        assertThrows(IllegalArgumentException::class.java) {
            Landmark(KeypointType.LEFT_KNEE, Point2D(0f, 0f), 1.1f)
        }
    }
}
