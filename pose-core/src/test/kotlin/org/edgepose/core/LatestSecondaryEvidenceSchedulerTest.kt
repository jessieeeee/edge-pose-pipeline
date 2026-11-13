package org.edgepose.core

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LatestSecondaryEvidenceSchedulerTest {
    @Test fun keepsOnlyLatestProgressWhileWorkIsRunning() {
        val queued = ArrayDeque<Runnable>()
        val evaluated = mutableListOf<Long>()
        val executor = Executor { queued.addLast(it) }
        val scheduler = LatestSecondaryEvidenceScheduler<Int, Int>(executor, { request ->
            evaluated += request.frameId
            request.payload
        }, { _, _ -> })
        assertEquals(SecondaryScheduleResult.ENQUEUED, scheduler.submit(request(1, .2f)))
        assertEquals(SecondaryScheduleResult.REPLACED, scheduler.submit(request(2, .4f)))
        assertEquals(SecondaryScheduleResult.IGNORED, scheduler.submit(request(3, .1f)))
        queued.removeFirst().run()
        assertEquals(listOf(2L), evaluated)
    }

    @Test fun evaluatorFailureDoesNotKillScheduler() {
        val queued = ArrayDeque<Runnable>()
        val failures = AtomicInteger()
        val executor = Executor { queued.addLast(it) }
        val scheduler = LatestSecondaryEvidenceScheduler<Int, Int>(executor, { error("secondary") }, { _, _ ->
            error("unreachable")
        }, { _, _ -> failures.incrementAndGet() })
        scheduler.submit(request(1, .1f))
        queued.removeFirst().run()
        assertEquals(1, failures.get())
        assertTrue(scheduler.submit(request(2, .2f)) != SecondaryScheduleResult.IGNORED)
    }

    private fun request(frameId: Long, progress: Float) = SecondaryEvidenceRequest(7L, frameId, progress, 1)
}
