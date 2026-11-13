package org.edgepose.core

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

data class SecondaryEvidenceRequest<T>(
    val attemptId: Long,
    val frameId: Long,
    val motionProgress: Float,
    val payload: T,
)

enum class SecondaryScheduleResult { ENQUEUED, REPLACED, IGNORED }

/** Serializes secondary-model work and drops stale samples to protect the primary loop. */
class LatestSecondaryEvidenceScheduler<T, R>(
    private val executor: Executor,
    private val evaluate: (SecondaryEvidenceRequest<T>) -> R,
    private val onResult: (SecondaryEvidenceRequest<T>, R) -> Unit,
    private val onFailure: (SecondaryEvidenceRequest<T>, Throwable) -> Unit = { _, _ -> },
) {
    private val lock = Any()
    private var pending: SecondaryEvidenceRequest<T>? = null
    private var running = false
    private val closed = AtomicBoolean(false)

    fun submit(request: SecondaryEvidenceRequest<T>): SecondaryScheduleResult {
        if (closed.get()) return SecondaryScheduleResult.IGNORED
        val shouldStart: Boolean
        val result: SecondaryScheduleResult
        synchronized(lock) {
            if (closed.get()) return SecondaryScheduleResult.IGNORED
            val current = pending
            val selected = when {
                current == null || current.attemptId != request.attemptId -> request
                request.motionProgress >= current.motionProgress -> request
                else -> current
            }
            result = when {
                selected === current -> SecondaryScheduleResult.IGNORED
                current != null -> SecondaryScheduleResult.REPLACED
                else -> SecondaryScheduleResult.ENQUEUED
            }
            if (result != SecondaryScheduleResult.IGNORED) pending = selected
            shouldStart = result != SecondaryScheduleResult.IGNORED && !running
            if (shouldStart) running = true
        }
        if (shouldStart) executor.execute(::drain)
        return result
    }

    fun close() {
        closed.set(true)
        synchronized(lock) { pending = null }
    }

    private fun drain() {
        while (true) {
            val request = synchronized(lock) {
                pending?.also { pending = null } ?: run { running = false; null }
            } ?: return
            try {
                onResult(request, evaluate(request))
            } catch (error: Throwable) {
                onFailure(request, error)
            }
        }
    }
}
