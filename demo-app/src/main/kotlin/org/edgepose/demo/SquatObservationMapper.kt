package org.edgepose.demo

import org.edgepose.core.Geometry
import org.edgepose.core.KeypointType
import org.edgepose.core.KneeAlignmentEvaluator
import org.edgepose.core.PoseFrame
import org.edgepose.core.SquatObservation
import org.edgepose.core.TrackingState

internal object SquatObservationMapper {
    fun map(frame: PoseFrame, tracking: TrackingState): SquatObservation? {
        if (tracking != TrackingState.TRACKING) {
            return SquatObservation(frame.timestampNanos, 180f, 0.5f, tracking)
        }
        val points = frame.landmarks
        val sides = listOf(
            Triple(KeypointType.LEFT_HIP, KeypointType.LEFT_KNEE, KeypointType.LEFT_ANKLE),
            Triple(KeypointType.RIGHT_HIP, KeypointType.RIGHT_KNEE, KeypointType.RIGHT_ANKLE),
        ).mapNotNull { (hipType, kneeType, ankleType) ->
            val hip = points[hipType]?.takeIf { it.confidence >= 0.30f }?.point ?: return@mapNotNull null
            val knee = points[kneeType]?.takeIf { it.confidence >= 0.30f }?.point ?: return@mapNotNull null
            val ankle = points[ankleType]?.takeIf { it.confidence >= 0.30f }?.point ?: return@mapNotNull null
            Geometry.angleDegrees(hip, knee, ankle)?.let { Triple(hip.y, ankle.y, it) }
        }
        if (sides.isEmpty()) return null
        return SquatObservation(
            timestampNanos = frame.timestampNanos,
            kneeAngleDegrees = sides.map { it.third }.average().toFloat(),
            hipY = sides.map { it.first }.average().toFloat(),
            trackingState = tracking,
            ankleY = sides.map { it.second }.average().toFloat(),
            kneeAlignment = KneeAlignmentEvaluator.evaluate(frame),
        )
    }
}
