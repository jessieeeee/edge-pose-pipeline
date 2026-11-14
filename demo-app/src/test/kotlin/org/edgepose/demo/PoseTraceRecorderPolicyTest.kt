package org.edgepose.demo

import org.junit.Assert.assertEquals
import org.junit.Test

class PoseTraceRecorderPolicyTest {
    @Test fun traceRetentionAndSizeAreBounded() {
        assertEquals(5L * 1024 * 1024, PoseTraceRecorder.MAX_TRACE_BYTES)
        assertEquals(5, PoseTraceRecorder.MAX_RETAINED_TRACES)
        assertEquals("pose-diagnostic-traces", PoseTraceRecorder.TRACE_DIRECTORY)
    }
}
