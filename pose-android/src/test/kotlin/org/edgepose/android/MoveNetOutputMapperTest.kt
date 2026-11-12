package org.edgepose.android

import org.edgepose.core.KeypointType
import org.junit.Assert.assertEquals
import org.junit.Test

class MoveNetOutputMapperTest {
    @Test
    fun mapsMoveNetYxScoreToCoreXY() {
        val rows = List(17) { index -> floatArrayOf(index / 10f, 0.8f, 0.6f) }
        val nose = MoveNetOutputMapper.map(rows)[KeypointType.NOSE]!!

        assertEquals(0.8f, nose.point.x, 0f)
        assertEquals(0f, nose.point.y, 0f)
        assertEquals(0.6f, nose.confidence, 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsWrongKeypointCount() {
        MoveNetOutputMapper.map(List(16) { floatArrayOf(0f, 0f, 1f) })
    }
}
