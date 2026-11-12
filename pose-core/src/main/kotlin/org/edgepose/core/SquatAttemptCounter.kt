package org.edgepose.core

/** A compact counter that preserves the squat attempt lifecycle used by the demo. */
class SquatAttemptCounter(
    private val standingAngleDegrees: Float = 170f,
    private val standingSamplesRequired: Int = 2,
    private val candidateKneeDropDegrees: Float = 8f,
    private val candidateRelativeHipDescent: Float = 0.04f,
    private val candidateStrongRelativeHipDescent: Float = 0.10f,
    private val lockKneeDropDegrees: Float = 18f,
    private val lockRelativeHipDescent: Float = 0.10f,
    private val bottomAngleDegrees: Float = 110f,
    private val ascentAngleDegrees: Float = 120f,
    private val ascentConfirmationDegrees: Float = 8f,
    private val returnStandingAngleDegrees: Float = 155f,
    private val settledStandingAngleDegrees: Float = 170f,
    private val maximumPreBottomDurationNanos: Long = 5_000_000_000L,
) {
    private enum class AttemptState { CANDIDATE, DESCENDING, BOTTOM, ASCENDING, RETURN_STANDING }

    private data class StandingSample(val angle: Float, val hipToAnkleSpan: Float)

    private val standingSamples = ArrayDeque<StandingSample>()
    private var state: AttemptState? = null
    private var baseline: StandingSample? = null
    private var acquiredAngle = 0f
    private var lowestAngle = 180f
    private var attemptStartedAt = 0L
    private var repetitions = 0
    private var lastTimestampNanos = -1L

    init {
        require(standingSamplesRequired > 0)
        require(maximumPreBottomDurationNanos >= 0L)
    }

    fun observe(observation: SquatObservation): AnalysisResult {
        require(observation.timestampNanos >= lastTimestampNanos) { "timestamps must be monotonic" }
        lastTimestampNanos = observation.timestampNanos
        val ankleY = observation.ankleY
        if (observation.trackingState != TrackingState.TRACKING || ankleY == null || ankleY <= observation.hipY) {
            abortAttempt()
            return AnalysisResult(SquatPhase.UP, repetitions, emptySet())
        }

        val sample = StandingSample(observation.kneeAngleDegrees, ankleY - observation.hipY)
        if (state == null) {
            observeStanding(sample)
            return AnalysisResult(SquatPhase.UP, repetitions, emptySet())
        }
        if (state in setOf(AttemptState.CANDIDATE, AttemptState.DESCENDING) &&
            observation.timestampNanos - attemptStartedAt > maximumPreBottomDurationNanos
        ) {
            abortAttempt()
            observeStanding(sample)
            return AnalysisResult(SquatPhase.UP, repetitions, emptySet())
        }

        val standing = checkNotNull(baseline)
        val relativeDescent = (standing.hipToAnkleSpan - sample.hipToAnkleSpan) / standing.hipToAnkleSpan
        val kneeDrop = standing.angle - sample.angle
        val current = checkNotNull(state)
        when (current) {
            AttemptState.CANDIDATE -> when {
                sample.angle >= settledStandingAngleDegrees && relativeDescent < lockRelativeHipDescent -> abortAttempt()
                relativeDescent >= lockRelativeHipDescent &&
                    (kneeDrop >= lockKneeDropDegrees || sample.angle < acquiredAngle) -> {
                    state = if (sample.angle <= bottomAngleDegrees) AttemptState.BOTTOM else AttemptState.DESCENDING
                    lowestAngle = sample.angle
                }
            }
            AttemptState.DESCENDING -> {
                lowestAngle = minOf(lowestAngle, sample.angle)
                when {
                    sample.angle <= bottomAngleDegrees -> state = AttemptState.BOTTOM
                    sample.angle - lowestAngle >= ascentConfirmationDegrees -> state =
                        if (sample.angle >= returnStandingAngleDegrees) AttemptState.RETURN_STANDING else AttemptState.ASCENDING
                }
            }
            AttemptState.BOTTOM -> {
                lowestAngle = minOf(lowestAngle, sample.angle)
                when {
                    sample.angle >= returnStandingAngleDegrees -> state = AttemptState.RETURN_STANDING
                    sample.angle >= ascentAngleDegrees -> state = AttemptState.ASCENDING
                }
            }
            AttemptState.ASCENDING -> if (sample.angle >= returnStandingAngleDegrees) state = AttemptState.RETURN_STANDING
            AttemptState.RETURN_STANDING -> if (sample.angle >= settledStandingAngleDegrees) {
                repetitions += 1
                abortAttempt()
                observeStanding(sample)
            }
        }
        return AnalysisResult(phase(), repetitions, emptySet())
    }

    private fun observeStanding(sample: StandingSample) {
        if (standingSamples.size < standingSamplesRequired) {
            if (sample.angle >= standingAngleDegrees) standingSamples.addLast(sample)
            return
        }
        val latest = standingSamples.toList()
        val baselineSample = StandingSample(
            angle = latest.map { it.angle }.average().toFloat(),
            hipToAnkleSpan = latest.map { it.hipToAnkleSpan }.average().toFloat(),
        )
        val candidateKneeDrop = baselineSample.angle - sample.angle
        val relativeDescent = (baselineSample.hipToAnkleSpan - sample.hipToAnkleSpan) / baselineSample.hipToAnkleSpan
        if (candidateKneeDrop >= candidateKneeDropDegrees && relativeDescent >= candidateRelativeHipDescent ||
            relativeDescent >= candidateStrongRelativeHipDescent
        ) {
            baseline = baselineSample
            acquiredAngle = sample.angle
            lowestAngle = sample.angle
            attemptStartedAt = lastTimestampNanos
            state = AttemptState.CANDIDATE
        } else if (sample.angle >= standingAngleDegrees && relativeDescent < candidateRelativeHipDescent) {
            standingSamples.addLast(sample)
            while (standingSamples.size > standingSamplesRequired) standingSamples.removeFirst()
        }
    }

    private fun phase(): SquatPhase = when (state) {
        AttemptState.CANDIDATE, AttemptState.DESCENDING -> SquatPhase.DESCENDING
        AttemptState.BOTTOM -> SquatPhase.BOTTOM
        AttemptState.ASCENDING, AttemptState.RETURN_STANDING -> SquatPhase.ASCENDING
        null -> SquatPhase.UP
    }

    private fun abortAttempt() {
        state = null
        baseline = null
        lowestAngle = 180f
        standingSamples.clear()
    }
}
