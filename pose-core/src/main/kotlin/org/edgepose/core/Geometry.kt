package org.edgepose.core

import kotlin.math.acos
import kotlin.math.sqrt

object Geometry {
    fun angleDegrees(first: Point2D, vertex: Point2D, last: Point2D): Float? {
        val firstX = first.x - vertex.x
        val firstY = first.y - vertex.y
        val lastX = last.x - vertex.x
        val lastY = last.y - vertex.y
        val firstLengthSquared = firstX * firstX + firstY * firstY
        val lastLengthSquared = lastX * lastX + lastY * lastY
        if (firstLengthSquared <= EPSILON || lastLengthSquared <= EPSILON) return null

        val dot = firstX * lastX + firstY * lastY
        val normalizedDot = (dot / sqrt(firstLengthSquared * lastLengthSquared)).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(normalizedDot.toDouble())).toFloat()
    }

    private const val EPSILON = 1e-12f
}
