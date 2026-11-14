package org.edgepose.core

/** Coordinates bounded secondary-model sampling for one squat attempt. */
class SecondaryEvidenceCoordinator<T, R>(
    private val scheduler: LatestSecondaryEvidenceScheduler<T, R>,
    private val evidenceOf: (R) -> KneeAlignmentEvidence?,
    private val onDiagnostic: (SecondaryEvidenceDiagnostic) -> Unit = {},
    private val maxSamplesPerAttempt: Int = 6,
    private val minimumSampleSpacingNanos: Long = 400_000_000L,
    private val deadlineNanos: Long = 2_000_000_000L,
) : AutoCloseable {
    private var attemptId: Long? = null
    private var startedAtNanos = 0L
    private var lastSampleAtNanos = Long.MIN_VALUE
    private var sampleCount = 0
    private var pendingEvidence: KneeAlignmentEvidence? = null
    private val reportedSkips = mutableSetOf<SecondaryEvidenceDiagnosticReason>()

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
            reportedSkips.clear()
        }
        val skipReason = when {
            phase == SquatPhase.UP -> SecondaryEvidenceDiagnosticReason.PHASE_INACTIVE
            sampleCount >= maxSamplesPerAttempt -> SecondaryEvidenceDiagnosticReason.SAMPLE_LIMIT
            timestampNanos - startedAtNanos > deadlineNanos -> SecondaryEvidenceDiagnosticReason.DEADLINE
            lastSampleAtNanos != Long.MIN_VALUE && timestampNanos - lastSampleAtNanos < minimumSampleSpacingNanos ->
                SecondaryEvidenceDiagnosticReason.MINIMUM_SPACING
            else -> null
        }
        if (skipReason != null) {
            if (reportedSkips.add(skipReason)) {
                onDiagnostic(SecondaryEvidenceDiagnostic(attempt, frameId, timestampNanos, phase, skipReason, sampleCount))
            }
            return null
        }
        lastSampleAtNanos = timestampNanos
        sampleCount++
        val scheduleResult = scheduler.submit(
            SecondaryEvidenceRequest(attempt, frameId, (timestampNanos - startedAtNanos).toFloat(), payload),
        )
        onDiagnostic(
            SecondaryEvidenceDiagnostic(
                attempt, frameId, timestampNanos, phase,
                when (scheduleResult) {
                    SecondaryScheduleResult.ENQUEUED -> SecondaryEvidenceDiagnosticReason.SCHEDULED
                    SecondaryScheduleResult.REPLACED -> SecondaryEvidenceDiagnosticReason.REPLACED
                    SecondaryScheduleResult.IGNORED -> SecondaryEvidenceDiagnosticReason.SCHEDULER_IGNORED
                },
                sampleCount,
            ),
        )
        return scheduleResult
    }

    @Synchronized
    fun takeEvidence(): KneeAlignmentEvidence? = pendingEvidence.also { pendingEvidence = null }

    @Synchronized
    fun acceptResult(result: R) {
        pendingEvidence = KneeEvidenceMerger.merge(pendingEvidence, evidenceOf(result))
    }

    override fun close() = scheduler.close()
}

enum class SecondaryEvidenceDiagnosticReason {
    PHASE_INACTIVE, SAMPLE_LIMIT, DEADLINE, MINIMUM_SPACING, SCHEDULED, REPLACED, SCHEDULER_IGNORED,
}

data class SecondaryEvidenceDiagnostic(
    val attemptId: Long,
    val frameId: Long,
    val timestampNanos: Long,
    val phase: SquatPhase,
    val reason: SecondaryEvidenceDiagnosticReason,
    val sampleCount: Int,
)
