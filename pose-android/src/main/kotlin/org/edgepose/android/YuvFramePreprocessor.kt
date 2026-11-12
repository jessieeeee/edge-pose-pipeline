package org.edgepose.android

import java.nio.ByteBuffer
import kotlin.math.roundToInt

/** Center-crops the upright image and produces packed RGB for the UINT8 MoveNet input. */
object YuvFramePreprocessor {
    fun convert(
        width: Int, height: Int,
        y: ByteBuffer, u: ByteBuffer, v: ByteBuffer,
        yRowStride: Int, uvRowStride: Int, uvPixelStride: Int,
        rotationDegrees: Int, outputSize: Int = 256,
    ): ByteArray {
        require(width > 0 && height > 0 && outputSize > 0)
        require(rotationDegrees in listOf(0, 90, 180, 270))
        val uprightWidth = if (rotationDegrees % 180 == 0) width else height
        val uprightHeight = if (rotationDegrees % 180 == 0) height else width
        val side = minOf(uprightWidth, uprightHeight)
        val left = (uprightWidth - side) / 2
        val top = (uprightHeight - side) / 2
        val rgb = ByteArray(outputSize * outputSize * 3)
        for (row in 0 until outputSize) for (column in 0 until outputSize) {
            val uprightX = left + ((column + 0.5f) * side / outputSize).toInt().coerceIn(0, side - 1)
            val uprightY = top + ((row + 0.5f) * side / outputSize).toInt().coerceIn(0, side - 1)
            val (sourceX, sourceY) = when (rotationDegrees) {
                90 -> uprightY to height - 1 - uprightX
                180 -> width - 1 - uprightX to height - 1 - uprightY
                270 -> width - 1 - uprightY to uprightX
                else -> uprightX to uprightY
            }
            val luma = y.get(sourceY * yRowStride + sourceX).toInt() and 0xff
            val chromaIndex = (sourceY / 2) * uvRowStride + (sourceX / 2) * uvPixelStride
            val cb = (u.get(chromaIndex).toInt() and 0xff) - 128
            val cr = (v.get(chromaIndex).toInt() and 0xff) - 128
            val base = (row * outputSize + column) * 3
            val scaled = 1.164f * (luma - 16).coerceAtLeast(0)
            rgb[base] = (scaled + 1.596f * cr).roundToInt().coerceIn(0, 255).toByte()
            rgb[base + 1] = (scaled - 0.392f * cb - 0.813f * cr).roundToInt().coerceIn(0, 255).toByte()
            rgb[base + 2] = (scaled + 2.017f * cb).roundToInt().coerceIn(0, 255).toByte()
        }
        return rgb
    }
}
