package org.edgepose.mediapipe

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import org.edgepose.core.KeypointType
import org.edgepose.core.Landmark
import java.util.concurrent.atomic.AtomicBoolean

/** Optional single-image MediaPipe adapter; callers own bitmap conversion and coordinate transforms. */
class MediaPipePoseEstimator(
    context: Context,
    modelAssetPath: String = DEFAULT_MODEL_ASSET,
) : AutoCloseable {
    private val closed = AtomicBoolean(false)
    private val landmarker = PoseLandmarker.createFromOptions(
        context,
        PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath(modelAssetPath).build())
            .setRunningMode(RunningMode.IMAGE)
            .setNumPoses(1)
            .build(),
    )

    @Synchronized
    fun estimate(bitmap: Bitmap): Map<KeypointType, Landmark> {
        check(!closed.get()) { "estimator is closed" }
        val result = landmarker.detect(BitmapImageBuilder(bitmap).build())
        val pose = result.landmarks().firstOrNull() ?: return emptyMap()
        return MediaPipeLandmarkMapper.map(
            pose.map { landmark ->
                NormalizedPoseLandmark(
                    x = landmark.x(),
                    y = landmark.y(),
                    visibility = landmark.visibility().orElse(0f),
                )
            },
        )
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) landmarker.close()
    }

    companion object {
        const val DEFAULT_MODEL_ASSET = "pose_landmarker.task"
    }
}
