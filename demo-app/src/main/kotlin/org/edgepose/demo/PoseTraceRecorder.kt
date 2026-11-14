package org.edgepose.demo

import android.content.Context
import java.io.File
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.edgepose.core.PoseTraceEvent
import org.edgepose.core.PoseTraceJson

internal class PoseTraceRecorder(context: Context) : AutoCloseable {
    private val directory = File(context.filesDir, TRACE_DIRECTORY).apply { mkdirs() }
    private val sessionId = UUID.randomUUID().toString()
    private val file = File(directory, "session-$sessionId.jsonl")
    private var sequence = 0L
    private val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "edge-pose-trace").apply { isDaemon = true }
    }

    init { pruneOldFiles() }

    @Synchronized
    fun record(frameId: Long, timestampNanos: Long, type: String, fields: Map<String, Any?> = emptyMap()) {
        if (closed) return
        val event = PoseTraceEvent(sessionId, sequence++, frameId, timestampNanos, type, fields)
        executor.execute {
            runCatching {
                val encoded = PoseTraceJson.encode(event)
                if (file.length() + encoded.toByteArray(Charsets.UTF_8).size <= MAX_TRACE_BYTES) {
                    file.appendText(encoded, Charsets.UTF_8)
                }
            }
        }
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        executor.shutdown()
    }

    @Volatile private var closed = false

    private fun pruneOldFiles() {
        directory.listFiles { candidate -> candidate.isFile && candidate.extension == "jsonl" }
            ?.sortedByDescending(File::lastModified)
            ?.drop(MAX_RETAINED_TRACES - 1)
            ?.forEach(File::delete)
    }

    companion object {
        const val TRACE_DIRECTORY = "pose-diagnostic-traces"
        const val MAX_TRACE_BYTES = 5L * 1024 * 1024
        const val MAX_RETAINED_TRACES = 5
    }
}
