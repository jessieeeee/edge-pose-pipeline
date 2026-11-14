package org.edgepose.core

import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Test

class SecondaryEvidenceCoordinatorDiagnosticsTest {
    @Test fun reportsSampleAndSkipReasons() {
        val events = mutableListOf<SecondaryEvidenceDiagnostic>()
        val scheduler = LatestSecondaryEvidenceScheduler<Int, Int>(Executor { }, { it.payload }, { _, _ -> })
        val coordinator = SecondaryEvidenceCoordinator(scheduler, { null }, onDiagnostic = events::add)
        coordinator.observeAttempt(1, 1, 1_000_000_000, SquatPhase.DESCENDING, 1)
        coordinator.observeAttempt(1, 2, 1_100_000_000, SquatPhase.DESCENDING, 2)
        assertEquals(SecondaryEvidenceDiagnosticReason.SCHEDULED, events[0].reason)
        assertEquals(SecondaryEvidenceDiagnosticReason.MINIMUM_SPACING, events[1].reason)
        assertEquals(1, events[1].attemptId)
        assertEquals(2, events[1].frameId)
        coordinator.observeAttempt(1, 3, 1_200_000_000, SquatPhase.DESCENDING, 3)
        assertEquals(2, events.size)
    }
}
