package org.edgepose.android

import org.edgepose.core.Point2D
import org.junit.Assert.assertEquals
import org.junit.Test

class MoveNetCoordinateMapperTest {
    @Test
    fun mapsPortraitCenterCrop() {
        val result = MoveNetCoordinateMapper.map(
            Point2D(0.5f, 0.25f), 0, false, 1080, 1920, sourceWidth = 1080, sourceHeight = 1920,
        )

        assertEquals(0.5f, result.x, 0.0001f)
        assertEquals(0.359375f, result.y, 0.0001f)
    }

    @Test
    fun rotatesAndMirrorsInViewerSpace() {
        val result = MoveNetCoordinateMapper.map(
            Point2D(0.2f, 0.3f), 90, true, 1000, 1000, sourceWidth = 1920, sourceHeight = 1080,
        )

        assertEquals(0.8f, result.x, 0.0001f)
        assertEquals(0.3875f, result.y, 0.0001f)
    }

    @Test
    fun restoresPortraitSourceCropBeforePreviewCropAndMirror() {
        val result = MoveNetCoordinateMapper.map(
            Point2D(0.5f, 0.5f), 0, true, 1080, 1920, sourceWidth = 1080, sourceHeight = 1920,
        )
        assertEquals(0.5f, result.x, 0.0001f)
        assertEquals(0.5f, result.y, 0.0001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidRotation() {
        MoveNetCoordinateMapper.map(Point2D(0.2f, 0.3f), 45, false, 1000, 1000)
    }
}
