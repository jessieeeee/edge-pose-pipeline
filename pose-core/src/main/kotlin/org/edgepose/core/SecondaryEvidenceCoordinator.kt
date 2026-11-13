package org.edgepose.core

/** Coordinates bounded secondary-model sampling for one squat attempt. */
class SecondaryEvidenceCoordinator<T, R>(
    private val scheduler: LatestSecondaryEvidenceScheduler<T, R>,
    private val evidenceOf: (R) -> KneeAlignmentEvidence?,
    private val maxSamplesPerAttempt: Int = 6,
    private val minimumSampleSpacingNanos: Long = 400_000_000L,
    private val deadlineNanos: Long = 2_000_000_000L,
) : AutoCloseable {
    private var attemptId: Long? = null
    private var startedAtNanos = 0L
    private var lastSampleAtNanos = Long.MIN_VALUE
    private var sampleCount = 0
    private var pendingEvidence: KneeAlignmentEvidence? = null

    init {
        require(maxSamplesPerAttempt > 0)
        require(minimumSampleSpacingNanos >= 0L)
        require(deadlineNanos >= 0L)
    }

    @Synchronized
    fun observeAttempt(
        attempt: Long,
        frameId: Long,
        timestampNanos: Long,
        phase: SquatPhase,
        payload: T,
    ): SecondaryScheduleResult? {
        if (attemptId != attempt) {
            attemptId = attempt
            startedAtNanos = timestampNanos
            lastSampleAtNanos = Long.MIN_VALUE
            sampleCount = 0
            pendingEvidence = null
        }
        if (phase == SquatPhase.UP || sampleCount >= maxSamplesPerAttempt ||
            timestampNanos - startedAtNanos > deadlineNanos ||
            (lastSampleAtNanos != Long.MIN_VALUE && timestampNanos - lastSampleAtNanos < minimumSampleSpacingNanos)
        ) return null
        lastSampleAtNanos = timestampNanos
        sampleCount++
        return scheduler.submit(SecondaryEvidenceRequest(attempt, frameId, timestampNanos.toFloat(), payload))
    }

    @Synchronized
    fun takeEvidence(): KneeAlignmentEvidence? = pendingEvidence.also { pendingEvidence = null }

    @Synchronized
    fun acceptResult(result: R) {
        pendingEvidence = KneeEvidenceMerger.merge(pendingEvidence, evidenceOf(result))
    }

    override fun close() = scheduler.close()
}
