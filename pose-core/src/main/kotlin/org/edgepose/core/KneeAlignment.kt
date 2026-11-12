package org.edgepose.core

import kotlin.math.abs
import kotlin.math.sign

data class KneeSideEvidence(val detected: Boolean, val strongDetected: Boolean)

data class KneeAlignmentEvidence(
    val left: KneeSideEvidence?,
    val right: KneeSideEvidence?,
) {
    val covered: Boolean get() = left != null && right != null
    val detected: Boolean get() = left?.detected == true || right?.detected == true
}

object KneeAlignmentEvaluator {
    fun evaluate(frame: PoseFrame): KneeAlignmentEvidence = KneeAlignmentEvidence(
        side(frame, KeypointType.LEFT_HIP, KeypointType.LEFT_KNEE, KeypointType.LEFT_ANKLE),
        side(frame, KeypointType.RIGHT_HIP, KeypointType.RIGHT_KNEE, KeypointType.RIGHT_ANKLE),
    )

    private fun side(frame: PoseFrame, hipType: KeypointType, kneeType: KeypointType, ankleType: KeypointType): KneeSideEvidence? {
        val hip = frame.landmarks[hipType]?.takeIf { it.confidence >= 0.30f }?.point ?: return null
        val knee = frame.landmarks[kneeType]?.takeIf { it.confidence >= 0.30f }?.point ?: return null
        val ankle = frame.landmarks[ankleType]?.takeIf { it.confidence >= 0.30f }?.point ?: return null
        val ankleToHip = ankle.x - hip.x
        val kneeToHip = knee.x - hip.x
        val crossed = ankleToHip != 0f && sign(ankleToHip) != sign(kneeToHip)
        val inwardDrift = ankleToHip != 0f && sign(ankleToHip) == sign(kneeToHip)
        val delta = abs(ankleToHip) - abs(kneeToHip)
        return KneeSideEvidence(
            detected = (crossed && abs(kneeToHip) > 0.015f) || (inwardDrift && delta > 0.020f),
            strongDetected = (crossed && abs(kneeToHip) > 0.080f) || (inwardDrift && delta > 0.050f),
        )
    }
}
