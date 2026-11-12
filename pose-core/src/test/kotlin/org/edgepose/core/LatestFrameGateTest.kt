package org.edgepose.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LatestFrameGateTest {
    @Test
    fun rejectsAndClosesWhileBusyThenReopensAfterLeaseClose() {
        val gate = LatestFrameGate<CloseCounter>()
        val first = CloseCounter()
        val rejected = CloseCounter()
        val next = CloseCounter()

        val lease = gate.tryAcquire(first)!!
        assertNull(gate.tryAcquire(rejected))
        assertEquals(1, rejected.closes)
        lease.close()
        lease.close()
        assertEquals(1, first.closes)

        gate.tryAcquire(next)!!.close()
        assertEquals(1, next.closes)
    }

    private class CloseCounter : AutoCloseable {
        var closes = 0
        override fun close() { closes += 1 }
    }
}
