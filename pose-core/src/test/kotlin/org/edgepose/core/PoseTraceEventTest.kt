package org.edgepose.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PoseTraceEventTest {
    @Test fun encodesVersionedJsonLineWithEscapedStringsAndNoImagePayload() {
        val event = PoseTraceEvent(
            sessionId = "session\"one",
            sequence = 3,
            frameId = 9,
            monotonicTimestampNanos = 10,
            type = "frame",
            fields = mapOf("note" to "line\nbreak", "count" to 2, "visible" to true),
        )
        val json = PoseTraceJson.encode(event)
        assertTrue(json.startsWith("{\"schemaVersion\":1,"))
        assertTrue(json.contains("\"sessionId\":\"session\\\"one\""))
        assertTrue(json.contains("\"note\":\"line\\nbreak\""))
        assertTrue(json.endsWith("}\n"))
        assertFalse(json.contains("bitmap"))
        assertEquals(1, json.count { it == '\n' })
    }
}
