package org.edgepose.core

enum class SquatPhase {
    UP,
    DESCENDING,
    BOTTOM,
    ASCENDING,
}

enum class FindingCode {
    SHALLOW_REP,
}

data class SquatConfig(
    val enterDescendingBelowDeg: Float = 140f,
    val enterBottomBelowDeg: Float = 100f,
    val returnToUpAboveDeg: Float = 160f,
    val minimumHipDropRatio: Float = 0.12f,
    val bottomHipDropRatio: Float = 0.25f,
    val transitionFrames: Int = 3,
    val transitionDurationNanos: Long = 100_000_000L,
) {
    init {
        require(enterBottomBelowDeg < enterDescendingBelowDeg)
        require(enterDescendingBelowDeg < returnToUpAboveDeg)
        require(minimumHipDropRatio > 0f)
        require(bottomHipDropRatio > minimumHipDropRatio)
        require(transitionFrames >= 1)
        require(transitionDurationNanos >= 0L)
    }
}

data class SquatObservation(
    val timestampNanos: Long,
    val kneeAngleDegrees: Float,
    val hipY: Float,
    val trackingState: TrackingState,
    val ankleY: Float? = null,
    val kneeAlignment: KneeAlignmentEvidence? = null,
)

data class AnalysisResult(
    val phase: SquatPhase,
    val repetitionCount: Int,
    val findings: Set<FindingCode>,
    val kneeAlignmentWarning: Boolean = false,
    val kneeAlignmentAlertCount: Int = 0,
    val kneeAlignmentCovered: Boolean = false,
)

class SquatPhaseEngine(
    private val config: SquatConfig = SquatConfig(),
    private val standingHipY: Float,
) {
    private var phase = SquatPhase.UP
    private var repetitionCount = 0
    private var lastTimestampNanos = -1L
    private var previousKneeAngle: Float? = null
    private var candidatePhase: SquatPhase? = null
    private var candidateStartedAtNanos = 0L
    private var candidateFrameCount = 0
    private var maximumHipDropRatio = 0f

    fun observe(observation: SquatObservation): AnalysisResult {
        require(observation.timestampNanos >= lastTimestampNanos) { "timestamps must be monotonic" }
        lastTimestampNanos = observation.timestampNanos
        val findings = mutableSetOf<FindingCode>()
        if (observation.trackingState != TrackingState.TRACKING) {
            previousKneeAngle = null
            clearCandidate()
            return result(findings)
        }

        val descendingMotion = previousKneeAngle?.let { observation.kneeAngleDegrees < it } ?: false
        val ascendingMotion = previousKneeAngle?.let { observation.kneeAngleDegrees > it } ?: false
        val hipDropRatio = observation.hipY - standingHipY
        val desiredPhase = when (phase) {
            SquatPhase.UP -> if (
                observation.kneeAngleDegrees < config.enterDescendingBelowDeg &&
                (descendingMotion || hipDropRatio >= config.minimumHipDropRatio)
            ) {
                SquatPhase.DESCENDING
            } else null
            SquatPhase.DESCENDING -> when {
                observation.kneeAngleDegrees < config.enterBottomBelowDeg || hipDropRatio >= config.bottomHipDropRatio ->
                    SquatPhase.BOTTOM
                ascendingMotion -> SquatPhase.ASCENDING
                else -> null
            }
            SquatPhase.BOTTOM -> if (ascendingMotion) SquatPhase.ASCENDING else null
            SquatPhase.ASCENDING -> if (observation.kneeAngleDegrees >= config.returnToUpAboveDeg) {
                SquatPhase.UP
            } else null
        }

        if (phase != SquatPhase.UP) {
            maximumHipDropRatio = maxOf(maximumHipDropRatio, hipDropRatio)
        }
        if (desiredPhase == null) {
            clearCandidate()
        } else if (confirm(desiredPhase, observation.timestampNanos)) {
            when {
                phase == SquatPhase.ASCENDING && desiredPhase == SquatPhase.UP -> {
                    repetitionCount += 1
                    if (maximumHipDropRatio < config.minimumHipDropRatio) findings += FindingCode.SHALLOW_REP
                    maximumHipDropRatio = 0f
                }
                phase == SquatPhase.UP && desiredPhase == SquatPhase.DESCENDING -> {
                    maximumHipDropRatio = maxOf(0f, hipDropRatio)
                }
            }
            phase = desiredPhase
            clearCandidate()
        }

        previousKneeAngle = observation.kneeAngleDegrees
        return result(findings)
    }

    private fun confirm(target: SquatPhase, timestampNanos: Long): Boolean {
        if (candidatePhase != target) {
            candidatePhase = target
            candidateStartedAtNanos = timestampNanos
            candidateFrameCount = 1
        } else {
            candidateFrameCount += 1
        }
        return candidateFrameCount >= config.transitionFrames &&
            timestampNanos - candidateStartedAtNanos >= config.transitionDurationNanos
    }

    private fun clearCandidate() {
        candidatePhase = null
        candidateFrameCount = 0
    }

    private fun result(findings: Set<FindingCode>) = AnalysisResult(phase, repetitionCount, findings)
}
