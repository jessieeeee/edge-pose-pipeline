package org.edgepose.demo

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoModeTest {
    @Test
    fun cameraModeNeverDisplaysSyntheticPoseOrReplayMetrics() {
        assertTrue(DemoMode.REPLAY.showSyntheticPose)
        assertTrue(DemoMode.REPLAY.showReplayMetrics)
        assertFalse(DemoMode.CAMERA.showSyntheticPose)
        assertFalse(DemoMode.CAMERA.showReplayMetrics)
    }
}
