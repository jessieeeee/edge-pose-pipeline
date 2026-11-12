package org.edgepose.demo

import org.edgepose.core.SquatPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class DemoReplayControllerTest {
    @Test
    fun replayAdvancesWithoutDuplicatingJudgmentLogic() {
        val controller = DemoReplayController()

        repeat(4) { controller.step() }

        assertEquals(SquatPhase.DESCENDING, controller.state.phase)
        assertEquals(4, controller.state.frameIndex)
    }

    @Test
    fun replayCanLoopWithMonotonicCoreTimestamps() {
        val controller = DemoReplayController()

        repeat(20) { controller.step() }

        assertEquals(0, controller.state.frameIndex)
    }
}
