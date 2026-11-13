package org.edgepose.mediapipe

import android.graphics.Bitmap
import org.edgepose.core.KneeAlignmentEvaluator
import org.edgepose.core.KneeAlignmentEvidence
import org.edgepose.core.PoseFrame

/** Converts one MediaPipe image result into the shared secondary knee evidence contract. */
class MediaPipeKneeEvidenceEvaluator(
    private val estimator: (Bitmap) -> PoseFrame?,
) {
    fun evaluate(bitmap: Bitmap, frameId: Long, timestampNanos: Long): KneeAlignmentEvidence? {
        val frame = estimator(bitmap) ?: return null
        return evaluateFrame(frame, frameId, timestampNanos)
    }

    fun evaluateFrame(frame: PoseFrame, frameId: Long, timestampNanos: Long): KneeAlignmentEvidence {
        val normalized = if (frame.frameId == frameId && frame.timestampNanos == timestampNanos) {
            frame
        } else {
            PoseFrame(frameId, timestampNanos, frame.landmarks)
        }
        return KneeAlignmentEvaluator.evaluate(normalized)
    }

    fun evaluateFrameOrNull(frame: PoseFrame?, frameId: Long, timestampNanos: Long): KneeAlignmentEvidence? =
        frame?.let { evaluateFrame(it, frameId, timestampNanos) }

    companion object {
        fun from(estimator: MediaPipePoseEstimator): MediaPipeKneeEvidenceEvaluator =
            MediaPipeKneeEvidenceEvaluator { bitmap ->
                val landmarks = estimator.estimate(bitmap)
                if (landmarks.isEmpty()) null else PoseFrame(0L, 0L, landmarks)
            }
    }
}
