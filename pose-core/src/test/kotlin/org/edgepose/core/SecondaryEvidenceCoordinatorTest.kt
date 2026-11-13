package org.edgepose.core

import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SecondaryEvidenceCoordinatorTest {
    @Test fun samplesOnlyAtSpacingAndStopsAtDeadline() {
        val queue = ArrayDeque<Runnable>()
        lateinit var coordinator: SecondaryEvidenceCoordinator<Int, Int>
        val scheduler = LatestSecondaryEvidenceScheduler<Int, Int>(Executor { queue.addLast(it) }, { it.payload }, { _, r -> coordinator.acceptResult(r) })
        coordinator = SecondaryEvidenceCoordinator(scheduler, { KneeAlignmentEvidence(null, null) }, maxSamplesPerAttempt = 2)
        assertEquals(SecondaryScheduleResult.ENQUEUED, coordinator.observeAttempt(1, 1, 0, SquatPhase.DESCENDING, 1))
        assertNull(coordinator.observeAttempt(1, 2, 100_000_000, SquatPhase.DESCENDING, 2))
        assertEquals(SecondaryScheduleResult.REPLACED, coordinator.observeAttempt(1, 3, 400_000_000, SquatPhase.BOTTOM, 3))
        assertNull(coordinator.observeAttempt(1, 4, 2_100_000_000, SquatPhase.ASCENDING, 4))
        queue.removeFirst().run()
    }
}
