package org.edgepose.android

import org.edgepose.core.KeypointType
import org.edgepose.core.Landmark
import org.edgepose.core.Point2D

/** Maps MoveNet's row-major [y, x, score] output into the core contract. */
object MoveNetOutputMapper {
    private val keypointIndices = linkedMapOf(
        KeypointType.NOSE to 0,
        KeypointType.LEFT_SHOULDER to 5,
        KeypointType.RIGHT_SHOULDER to 6,
        KeypointType.LEFT_ELBOW to 7,
        KeypointType.RIGHT_ELBOW to 8,
        KeypointType.LEFT_WRIST to 9,
        KeypointType.RIGHT_WRIST to 10,
        KeypointType.LEFT_HIP to 11,
        KeypointType.RIGHT_HIP to 12,
        KeypointType.LEFT_KNEE to 13,
        KeypointType.RIGHT_KNEE to 14,
        KeypointType.LEFT_ANKLE to 15,
        KeypointType.RIGHT_ANKLE to 16,
    )

    fun map(rows: List<FloatArray>): Map<KeypointType, Landmark> {
        require(rows.size == 17) { "MoveNet must contain 17 keypoints" }
        rows.forEach { row ->
            require(row.size == 3) { "MoveNet row must be [y, x, score]" }
            require(row.all { it.isFinite() }) { "MoveNet output must be finite" }
        }
        return keypointIndices.map { (type, index) ->
            val row = rows[index]
            type to Landmark(type, Point2D(row[1], row[0]), row[2].coerceIn(0f, 1f))
        }.toMap()
    }
}
