package org.edgepose.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SquatPhaseEngineTest {
    @Test
    fun countsRepAfterDownAndUpAndEmitsShallowFinding() {
        val engine = SquatPhaseEngine(standingHipY = 0.5f)

        assertEquals(SquatPhase.UP, engine.observe(observation(0, 170f, 0.5f)).phase)
        engine.observe(observation(40, 138f, 0.54f))
        engine.observe(observation(80, 136f, 0.55f))
        assertEquals(SquatPhase.DESCENDING, engine.observe(observation(140, 135f, 0.56f)).phase)
        engine.observe(observation(160, 145f, 0.56f))
        engine.observe(observation(200, 150f, 0.55f))
        assertEquals(SquatPhase.ASCENDING, engine.observe(observation(260, 155f, 0.54f)).phase)
        engine.observe(observation(280, 165f, 0.52f))
        engine.observe(observation(320, 165f, 0.52f))
        val completed = engine.observe(observation(380, 165f, 0.52f))

        assertEquals(1, completed.repetitionCount)
        assertTrue(completed.findings.contains(FindingCode.SHALLOW_REP))
        assertEquals(SquatPhase.UP, completed.phase)
    }

    @Test
    fun pausesTransitionsWhenTrackingIsInvalid() {
        val engine = SquatPhaseEngine(standingHipY = 0.5f)
        engine.observe(observation(0, 170f, 0.5f))

        val result = engine.observe(observation(100, 120f, 0.7f, TrackingState.LOW_CONFIDENCE))

        assertEquals(SquatPhase.UP, result.phase)
        assertEquals(0, result.repetitionCount)
    }

    @Test
    fun rejectsSingleFramePhaseSpike() {
        val engine = SquatPhaseEngine(standingHipY = 0.5f)
        engine.observe(observation(0, 170f, 0.5f))

        val result = engine.observe(observation(100, 120f, 0.7f))

        assertEquals(SquatPhase.UP, result.phase)
        assertEquals(0, result.repetitionCount)
    }

    @Test
    fun reachesBottomWhenConfiguredDepthIsSustained() {
        val engine = SquatPhaseEngine(standingHipY = 0.5f)
        engine.observe(observation(0, 170f, 0.5f))
        engine.observe(observation(40, 138f, 0.54f))
        engine.observe(observation(80, 136f, 0.55f))
        engine.observe(observation(140, 135f, 0.56f))
        engine.observe(observation(160, 130f, 0.56f))
        val descending = engine.observe(observation(200, 125f, 0.56f))
        assertEquals(SquatPhase.DESCENDING, descending.phase)

        engine.observe(observation(240, 95f, 0.56f))
        engine.observe(observation(280, 94f, 0.57f))
        val bottom = engine.observe(observation(340, 93f, 0.58f))

        assertEquals(SquatPhase.BOTTOM, bottom.phase)
    }

    private fun observation(time: Long, knee: Float, hip: Float, tracking: TrackingState = TrackingState.TRACKING) =
        SquatObservation(time * 1_000_000, knee, hip, tracking)
}
