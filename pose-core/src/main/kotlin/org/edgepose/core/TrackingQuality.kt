package org.edgepose.core

enum class TrackingState {
    NO_PERSON,
    LOW_CONFIDENCE,
    PARTIAL_BODY,
    TRACKING,
}

data class TrackingConfig(
    val minimumConfidence: Float = 0.45f,
    val requiredLowerBodyPoints: Int = 3,
) {
    init {
        require(minimumConfidence in 0f..1f)
        require(requiredLowerBodyPoints in 1..6)
    }
}

class TrackingQualityEvaluator(private val config: TrackingConfig = TrackingConfig()) {
    fun evaluate(frame: PoseFrame?): TrackingState {
        if (frame == null || frame.landmarks.isEmpty()) return TrackingState.NO_PERSON

        val lowerBody = LOWER_BODY.mapNotNull(frame.landmarks::get)
        if (lowerBody.isEmpty()) return TrackingState.PARTIAL_BODY
        val confidentCount = lowerBody.count { it.confidence >= config.minimumConfidence }
        if (confidentCount < config.requiredLowerBodyPoints) {
            return if (lowerBody.any { it.confidence > 0f }) {
                TrackingState.LOW_CONFIDENCE
            } else {
                TrackingState.PARTIAL_BODY
            }
        }
        return TrackingState.TRACKING
    }

    private companion object {
        val LOWER_BODY = listOf(
            KeypointType.LEFT_HIP,
            KeypointType.RIGHT_HIP,
            KeypointType.LEFT_KNEE,
            KeypointType.RIGHT_KNEE,
            KeypointType.LEFT_ANKLE,
            KeypointType.RIGHT_ANKLE,
        )
    }
}
