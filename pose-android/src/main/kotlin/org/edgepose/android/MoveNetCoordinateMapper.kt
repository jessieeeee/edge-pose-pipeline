package org.edgepose.android

import org.edgepose.core.Point2D

/** Converts normalized points from the center-square model crop into front-camera preview space. */
object MoveNetCoordinateMapper {
    fun map(
        point: Point2D,
        rotationDegrees: Int,
        frontCamera: Boolean,
        previewWidth: Int,
        previewHeight: Int,
        sourceWidth: Int = previewWidth,
        sourceHeight: Int = previewHeight,
    ): Point2D {
        require(previewWidth > 0 && previewHeight > 0 && sourceWidth > 0 && sourceHeight > 0)
        val normalizedRotation = (rotationDegrees % 360 + 360) % 360
        require(normalizedRotation in setOf(0, 90, 180, 270)) { "rotationDegrees must be a multiple of 90" }
        val uprightWidth = if (normalizedRotation % 180 == 0) sourceWidth else sourceHeight
        val uprightHeight = if (normalizedRotation % 180 == 0) sourceHeight else sourceWidth
        val side = minOf(uprightWidth, uprightHeight).toFloat()
        val cropX = (uprightWidth - side) / 2f
        val cropY = (uprightHeight - side) / 2f
        val upright = Point2D(
            (cropX + point.x * side) / uprightWidth,
            (cropY + point.y * side) / uprightHeight,
        )
        val oriented = upright
        val previewCrop = if (previewWidth < previewHeight) {
            Point2D(oriented.x, oriented.y)
        } else if (previewWidth > previewHeight) {
            val offset = (previewWidth - previewHeight).toFloat() / 2f
            Point2D((oriented.x * uprightWidth - offset) / previewHeight, oriented.y)
        } else oriented
        return if (frontCamera) Point2D(1f - previewCrop.x, previewCrop.y) else previewCrop
    }
}
