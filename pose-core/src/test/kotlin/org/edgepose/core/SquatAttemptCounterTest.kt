package org.edgepose.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SquatAttemptCounterTest {
    @Test
    fun sustainedUnilateralRiskCountsOnceAfterCompletedRep() {
        val counter = SquatAttemptCounter()
        val risk = KneeAlignmentEvidence(KneeSideEvidence(true, false), KneeSideEvidence(false, false))
        counter.observe(observation(0, 176f, 0.20f, 0.80f))
        counter.observe(observation(120, 174f, 0.20f, 0.80f))
        counter.observe(observation(240, 160f, 0.24f, 0.80f))
        counter.observe(observation(360, 145f, 0.27f, 0.80f).copy(kneeAlignment = risk))
        val bottom = counter.observe(observation(480, 105f, 0.34f, 0.80f).copy(kneeAlignment = risk))
        assertEquals(true, bottom.kneeAlignmentWarning)
        assertEquals(0, bottom.kneeAlignmentAlertCount)
        counter.observe(observation(600, 122f, 0.28f, 0.80f).copy(kneeAlignment = risk))
        counter.observe(observation(720, 156f, 0.22f, 0.80f))
        val completed = counter.observe(observation(840, 172f, 0.20f, 0.80f))
        assertEquals(1, completed.repetitionCount)
        assertEquals(1, completed.kneeAlignmentAlertCount)
    }

    @Test
    fun briefRiskAndTrackingLossNeverCountAnAlert() {
        val counter = SquatAttemptCounter()
        val risk = KneeAlignmentEvidence(KneeSideEvidence(true, false), KneeSideEvidence(false, false))
        counter.observe(observation(0, 176f, 0.20f, 0.80f))
        counter.observe(observation(120, 174f, 0.20f, 0.80f))
        counter.observe(observation(240, 160f, 0.24f, 0.80f))
        counter.observe(observation(360, 145f, 0.27f, 0.80f).copy(kneeAlignment = risk))
        val lost = counter.observe(SquatObservation(400_000_000, 100f, 0.35f, TrackingState.LOW_CONFIDENCE))
        assertEquals(false, lost.kneeAlignmentWarning)
        assertEquals(0, lost.kneeAlignmentAlertCount)
    }

    @Test fun uncoveredFrameHidesWarningButPreservesSupportedAttemptEvidence() {
        val counter = SquatAttemptCounter()
        val risk = KneeAlignmentEvidence(KneeSideEvidence(true, false), KneeSideEvidence(false, false))
        counter.observe(observation(0, 176f, .20f, .80f))
        counter.observe(observation(120, 174f, .20f, .80f))
        counter.observe(observation(240, 160f, .24f, .80f))
        counter.observe(observation(360, 145f, .27f, .80f).copy(kneeAlignment = risk))
        counter.observe(observation(480, 105f, .34f, .80f).copy(kneeAlignment = risk))
        val uncovered = counter.observe(observation(600, 122f, .28f, .80f).copy(
            kneeAlignment = KneeAlignmentEvidence(risk.left, null),
        ))
        assertEquals(false, uncovered.kneeAlignmentWarning)
        assertEquals(false, uncovered.kneeAlignmentCovered)
        counter.observe(observation(720, 156f, .22f, .80f))
        assertEquals(1, counter.observe(observation(840, 172f, .20f, .80f)).kneeAlignmentAlertCount)
    }

    @Test fun briefRiskWithoutSupportDoesNotCountOnCompletedRep() {
        val counter = SquatAttemptCounter()
        val risk = KneeAlignmentEvidence(KneeSideEvidence(true, false), KneeSideEvidence(false, false))
        val clear = KneeAlignmentEvidence(KneeSideEvidence(false, false), KneeSideEvidence(false, false))
        counter.observe(observation(0, 176f, .20f, .80f))
        counter.observe(observation(120, 174f, .20f, .80f))
        counter.observe(observation(240, 160f, .24f, .80f))
        counter.observe(observation(360, 145f, .27f, .80f).copy(kneeAlignment = risk))
        counter.observe(observation(400, 105f, .34f, .80f).copy(kneeAlignment = clear))
        counter.observe(observation(500, 122f, .28f, .80f).copy(kneeAlignment = clear))
        counter.observe(observation(600, 156f, .22f, .80f).copy(kneeAlignment = clear))
        val completed = counter.observe(observation(700, 172f, .20f, .80f).copy(kneeAlignment = clear))
        assertEquals(1, completed.repetitionCount)
        assertEquals(0, completed.kneeAlignmentAlertCount)
    }

    @Test
    fun calibratesStandingThenCountsOnlyAfterReturningToStanding() {
        val counter = SquatAttemptCounter()

        assertEquals(SquatPhase.UP, counter.observe(observation(0, 176f, 0.20f, 0.80f)).phase)
        assertEquals(SquatPhase.UP, counter.observe(observation(120, 174f, 0.20f, 0.80f)).phase)
        counter.observe(observation(240, 160f, 0.24f, 0.80f))
        assertEquals(SquatPhase.DESCENDING, counter.observe(observation(360, 145f, 0.27f, 0.80f)).phase)
        assertEquals(SquatPhase.BOTTOM, counter.observe(observation(480, 105f, 0.34f, 0.80f)).phase)
        assertEquals(SquatPhase.ASCENDING, counter.observe(observation(600, 122f, 0.28f, 0.80f)).phase)
        assertEquals(SquatPhase.ASCENDING, counter.observe(observation(720, 156f, 0.22f, 0.80f)).phase)

        val completed = counter.observe(observation(840, 172f, 0.20f, 0.80f))
        assertEquals(1, completed.repetitionCount)
    }

    @Test
    fun doesNotCountWhenTrackingIsLostBeforeStandingReturn() {
        val counter = SquatAttemptCounter()
        counter.observe(observation(0, 176f, 0.20f, 0.80f))
        counter.observe(observation(120, 174f, 0.20f, 0.80f))
        counter.observe(observation(240, 160f, 0.24f, 0.80f))
        counter.observe(observation(360, 145f, 0.27f, 0.80f))

        val lost = counter.observe(SquatObservation(480_000_000, 100f, 0.35f, TrackingState.LOW_CONFIDENCE))

        assertEquals(0, lost.repetitionCount)
        assertEquals(SquatPhase.UP, lost.phase)
    }

    private fun observation(timeMs: Long, knee: Float, hipY: Float, ankleY: Float) =
        SquatObservation(timeMs * 1_000_000, knee, hipY, TrackingState.TRACKING, ankleY)
}
