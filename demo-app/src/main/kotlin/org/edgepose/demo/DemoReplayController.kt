package org.edgepose.demo

import org.edgepose.core.SquatObservation
import org.edgepose.core.SquatPhase
import org.edgepose.core.SquatPhaseEngine
import org.edgepose.core.TrackingState
import org.edgepose.core.KeypointType
import org.edgepose.core.Landmark
import org.edgepose.core.Point2D
import org.edgepose.core.PoseFrame

data class DemoUiState(
    val phase: SquatPhase = SquatPhase.UP,
    val repetitions: Int = 0,
    val tracking: TrackingState = TrackingState.TRACKING,
    val frameIndex: Int = 0,
    val latencyMs: Int = 0,
    val running: Boolean = false,
)

class DemoReplayController {
    private var engine = newEngine()
    private val samples = listOf(
        170f to 0.50f, 138f to 0.54f, 136f to 0.55f, 135f to 0.56f,
        145f to 0.56f, 150f to 0.55f, 155f to 0.54f, 165f to 0.52f,
        165f to 0.52f, 165f to 0.52f,
    )
    private var index = 0
    private var frameCounter = 0L
    var state = DemoUiState()
        private set

    fun reset() {
        index = 0
        frameCounter = 0L
        engine = newEngine()
        state = DemoUiState()
    }

    fun step(): DemoUiState {
        val (knee, hip) = samples[index]
        val result = engine.observe(
            SquatObservation(
                timestampNanos = frameCounter * 60_000_000L,
                kneeAngleDegrees = knee,
                hipY = hip,
                trackingState = TrackingState.TRACKING,
            ),
        )
        frameCounter += 1
        index = (index + 1) % samples.size
        state = state.copy(
            phase = result.phase,
            repetitions = result.repetitionCount,
            frameIndex = index,
            latencyMs = 12 + (index % 4),
            running = true,
        )
        return state
    }

    fun syntheticPoseFrame(): PoseFrame {
        val hipY = samples[(index - 1 + samples.size) % samples.size].second
        val kneeBend = ((hipY - 0.5f).coerceIn(0f, 0.08f) * 2f)
        val points = mapOf(
            KeypointType.LEFT_SHOULDER to Point2D(0.40f, 0.25f),
            KeypointType.RIGHT_SHOULDER to Point2D(0.60f, 0.25f),
            KeypointType.LEFT_HIP to Point2D(0.43f, hipY),
            KeypointType.RIGHT_HIP to Point2D(0.57f, hipY),
            KeypointType.LEFT_KNEE to Point2D(0.40f - kneeBend, 0.69f),
            KeypointType.RIGHT_KNEE to Point2D(0.60f + kneeBend, 0.69f),
            KeypointType.LEFT_ANKLE to Point2D(0.38f, 0.91f),
            KeypointType.RIGHT_ANKLE to Point2D(0.62f, 0.91f),
        ).mapValues { (type, point) -> Landmark(type, point, 0.95f) }
        return PoseFrame(frameCounter, frameCounter * 60_000_000L, points)
    }

    private fun newEngine() = SquatPhaseEngine(standingHipY = 0.5f)
}
