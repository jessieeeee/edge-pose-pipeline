package org.edgepose.core

enum class KeypointType {
    NOSE,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE,
}

data class Point2D(val x: Float, val y: Float)

data class Landmark(
    val type: KeypointType,
    val point: Point2D,
    val confidence: Float,
) {
    init {
        require(confidence in 0f..1f) { "confidence must be between 0 and 1" }
    }
}

data class PoseFrame(
    val frameId: Long,
    val timestampNanos: Long,
    val landmarks: Map<KeypointType, Landmark>,
) {
    init {
        require(frameId >= 0) { "frameId must not be negative" }
        require(timestampNanos >= 0) { "timestampNanos must not be negative" }
        require(landmarks.all { (type, landmark) -> type == landmark.type }) {
            "landmark map keys must match landmark types"
        }
    }
}
