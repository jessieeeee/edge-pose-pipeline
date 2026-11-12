package org.edgepose.core

import java.util.concurrent.atomic.AtomicBoolean

/** Admits one frame at a time and releases rejected or completed frames exactly once. */
class LatestFrameGate<T : AutoCloseable> {
    private val busy = AtomicBoolean(false)

    fun tryAcquire(frame: T): Lease<T>? {
        if (!busy.compareAndSet(false, true)) {
            frame.close()
            return null
        }
        return Lease(frame) { busy.set(false) }
    }

    class Lease<T : AutoCloseable> internal constructor(
        val frame: T,
        private val releaseGate: () -> Unit,
    ) : AutoCloseable {
        private val closed = AtomicBoolean(false)

        override fun close() {
            if (closed.compareAndSet(false, true)) {
                try {
                    frame.close()
                } finally {
                    releaseGate()
                }
            }
        }
    }
}
