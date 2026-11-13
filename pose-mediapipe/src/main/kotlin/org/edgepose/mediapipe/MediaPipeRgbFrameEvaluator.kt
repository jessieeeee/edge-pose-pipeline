package org.edgepose.mediapipe

import android.graphics.Bitmap
import android.graphics.Color
import org.edgepose.core.KneeAlignmentEvidence

/** Evaluates the packed RGB UINT8 frame already prepared for MoveNet. */
class MediaPipeRgbFrameEvaluator(
    private val evaluator: MediaPipeKneeEvidenceEvaluator,
    private val inputSize: Int = 256,
) {
    init { require(inputSize > 0) }

    fun evaluate(rgb: ByteArray, frameId: Long, timestampNanos: Long): KneeAlignmentEvidence? {
        require(rgb.size == inputSize * inputSize * 3) { "RGB frame must be ${inputSize}x${inputSize}" }
        val pixels = IntArray(inputSize * inputSize)
        var offset = 0
        for (index in pixels.indices) {
            val red = rgb[offset++].toInt() and 0xff
            val green = rgb[offset++].toInt() and 0xff
            val blue = rgb[offset++].toInt() and 0xff
            pixels[index] = Color.rgb(red, green, blue)
        }
        val bitmap = Bitmap.createBitmap(pixels, inputSize, inputSize, Bitmap.Config.ARGB_8888)
        return evaluator.evaluate(bitmap, frameId, timestampNanos)
    }
}
