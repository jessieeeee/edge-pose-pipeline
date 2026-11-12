package org.edgepose.demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import org.edgepose.core.KeypointType
import org.edgepose.core.PoseFrame

class PoseOverlayView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(94, 234, 212)
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private var poseFrame: PoseFrame? = null

    fun render(frame: PoseFrame?) {
        poseFrame = frame
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val points = poseFrame?.landmarks ?: return
        val edges = listOf(
            KeypointType.LEFT_SHOULDER to KeypointType.RIGHT_SHOULDER,
            KeypointType.LEFT_SHOULDER to KeypointType.LEFT_HIP,
            KeypointType.RIGHT_SHOULDER to KeypointType.RIGHT_HIP,
            KeypointType.LEFT_HIP to KeypointType.RIGHT_HIP,
            KeypointType.LEFT_HIP to KeypointType.LEFT_KNEE,
            KeypointType.RIGHT_HIP to KeypointType.RIGHT_KNEE,
            KeypointType.LEFT_KNEE to KeypointType.LEFT_ANKLE,
            KeypointType.RIGHT_KNEE to KeypointType.RIGHT_ANKLE,
        )
        edges.forEach { (from, to) ->
            val start = points[from]?.takeIf { it.confidence >= 0.45f }?.point ?: return@forEach
            val end = points[to]?.takeIf { it.confidence >= 0.45f }?.point ?: return@forEach
            canvas.drawLine(start.x * width, start.y * height, end.x * width, end.y * height, paint)
        }
    }
}
