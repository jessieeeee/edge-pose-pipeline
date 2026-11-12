package org.edgepose.mediapipe

import org.edgepose.core.KeypointType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MediaPipeLandmarkMapperTest {
    @Test
    fun mapsNormalizedCoordinatesAndVisibilityToCoreLandmarks() {
        val points = List(33) { index -> NormalizedPoseLandmark(index / 100f, 0.75f, 0.9f) }

        val mapped = MediaPipeLandmarkMapper.map(points)

        assertEquals(0.11f, mapped.getValue(KeypointType.LEFT_SHOULDER).point.x, 0f)
        assertEquals(0.75f, mapped.getValue(KeypointType.LEFT_SHOULDER).point.y, 0f)
        assertEquals(0.9f, mapped.getValue(KeypointType.LEFT_SHOULDER).confidence, 0f)
        assertEquals(0.25f, mapped.getValue(KeypointType.LEFT_KNEE).point.x, 0f)
    }

    @Test
    fun omitsLandmarksWithNegativeVisibilityAndClampsConfidence() {
        val points = MutableList(33) { NormalizedPoseLandmark(0.4f, 0.6f, 1.2f) }
        points[25] = points[25].copy(visibility = -1f)

        val mapped = MediaPipeLandmarkMapper.map(points)

        assertFalse(mapped.containsKey(KeypointType.LEFT_KNEE))
        assertEquals(1f, mapped.getValue(KeypointType.LEFT_HIP).confidence, 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIncompleteLandmarkSet() {
        MediaPipeLandmarkMapper.map(List(32) { NormalizedPoseLandmark(0f, 0f, 1f) })
    }
}
