package org.edgepose.mediapipe

import org.edgepose.core.KeypointType
import org.edgepose.core.Landmark
import org.edgepose.core.Point2D

data class NormalizedPoseLandmark(
    val x: Float,
    val y: Float,
    val visibility: Float,
)

/** Maps MediaPipe's 33-point pose order into the shared subset used by pose-core. */
object MediaPipeLandmarkMapper {
    private val landmarkIndices = linkedMapOf(
        KeypointType.NOSE to 0,
        KeypointType.LEFT_SHOULDER to 11,
        KeypointType.RIGHT_SHOULDER to 12,
        KeypointType.LEFT_ELBOW to 13,
        KeypointType.RIGHT_ELBOW to 14,
        KeypointType.LEFT_WRIST to 15,
        KeypointType.RIGHT_WRIST to 16,
        KeypointType.LEFT_HIP to 23,
        KeypointType.RIGHT_HIP to 24,
        KeypointType.LEFT_KNEE to 25,
        KeypointType.RIGHT_KNEE to 26,
        KeypointType.LEFT_ANKLE to 27,
        KeypointType.RIGHT_ANKLE to 28,
    )

    fun map(landmarks: List<NormalizedPoseLandmark>): Map<KeypointType, Landmark> {
        require(landmarks.size >= 33) { "MediaPipe pose must contain at least 33 landmarks" }
        return landmarkIndices.mapNotNull { (type, index) ->
            val source = landmarks[index]
            require(source.x.isFinite() && source.y.isFinite() && source.visibility.isFinite()) {
                "MediaPipe landmark values must be finite"
            }
            if (source.visibility < 0f) return@mapNotNull null
            type to Landmark(
                type = type,
                point = Point2D(source.x, source.y),
                confidence = source.visibility.coerceIn(0f, 1f),
            )
        }.toMap()
    }
}
