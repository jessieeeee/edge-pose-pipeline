package org.edgepose.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.ComponentActivity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.io.File
import org.edgepose.android.MoveNetLiteRtEstimator
import org.edgepose.android.MoveNetCoordinateMapper
import org.edgepose.android.YuvFramePreprocessor
import org.edgepose.core.PoseFrame
import org.edgepose.core.SquatPhaseEngine
import org.edgepose.core.SquatObservation
import org.edgepose.core.TrackingQualityEvaluator
import org.edgepose.core.TrackingState
import org.edgepose.core.Geometry
import org.edgepose.core.KeypointType

class MainActivity : ComponentActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var overlayView: PoseOverlayView
    private lateinit var cameraStatus: TextView
    private lateinit var metrics: TextView
    private lateinit var cameraButton: Button
    private lateinit var modeButton: Button
    private lateinit var replayButton: Button
    private lateinit var subtitle: TextView
    private lateinit var cameraExecutor: ExecutorService
    private val replayController = DemoReplayController()
    private var mode = DemoMode.REPLAY
    private var cameraProvider: ProcessCameraProvider? = null
    private var estimator: MoveNetLiteRtEstimator? = null
    private var frameId = 0L
    private var squatEngine = SquatPhaseEngine(standingHipY = 0.5f)
    private val trackingEvaluator = TrackingQualityEvaluator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = 0xFF0B1220.toInt()
        window.navigationBarColor = 0xFF0B1220.toInt()
        cameraExecutor = Executors.newSingleThreadExecutor()
        buildContent()
        updateReplayMetrics()
        showMode()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST && mode == DemoMode.CAMERA) refreshCameraState()
    }

    override fun onDestroy() {
        cameraProvider?.unbindAll()
        estimator?.close()
        estimator = null
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
        super.onDestroy()
    }

    private fun buildContent() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(0xFF0B1220.toInt())
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(20, bars.top + 24, 20, bars.bottom + 20)
            insets
        }
        root.addView(TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 25f
            setTextColor(0xFFFFFFFF.toInt())
        })
        subtitle = TextView(this).apply {
            text = getString(R.string.demo_subtitle)
            textSize = 12f
            setTextColor(0xFFA3B1C9.toInt())
        }
        root.addView(subtitle)
        modeButton = Button(this).apply {
            setOnClickListener {
                mode = if (mode == DemoMode.REPLAY) DemoMode.CAMERA else DemoMode.REPLAY
                showMode()
            }
        }
        root.addView(modeButton, LinearLayout.LayoutParams(-1, -2))

        val cameraContainer = android.widget.FrameLayout(this).apply {
            setBackgroundColor(0xFF1B2B44.toInt())
        }
        previewView = PreviewView(this).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
        overlayView = PoseOverlayView(this)
        cameraContainer.addView(previewView, android.widget.FrameLayout.LayoutParams(-1, -1))
        cameraContainer.addView(overlayView, android.widget.FrameLayout.LayoutParams(-1, -1))
        root.addView(cameraContainer, LinearLayout.LayoutParams(-1, 0, 0.82f).apply {
            topMargin = 18
            bottomMargin = 12
        })

        cameraStatus = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFD9E2F2.toInt())
            gravity = Gravity.CENTER
        }
        root.addView(cameraStatus, LinearLayout.LayoutParams(-1, -2))
        cameraButton = Button(this).apply {
            setText(R.string.request_camera)
            setOnClickListener { requestCameraPermission() }
        }
        root.addView(cameraButton, LinearLayout.LayoutParams(-1, -2))
        metrics = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 12, 0, 8)
        }
        root.addView(metrics, LinearLayout.LayoutParams(-1, -2))
        replayButton = Button(this).apply {
            setText(R.string.step_replay)
            setOnClickListener {
                replayController.step()
                updateReplayMetrics()
            }
        }
        root.addView(replayButton, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 4 })
        setContentView(root)
    }

    private fun showMode() {
        if (mode == DemoMode.REPLAY) {
            subtitle.setText(R.string.demo_subtitle)
            cameraProvider?.unbindAll()
            estimator?.close()
            estimator = null
            squatEngine = SquatPhaseEngine(standingHipY = 0.5f)
            previewView.visibility = View.INVISIBLE
            cameraButton.visibility = View.GONE
            cameraStatus.visibility = View.GONE
            replayButton.visibility = View.VISIBLE
            modeButton.setText(R.string.open_camera)
            updateReplayMetrics()
        } else {
            subtitle.setText(R.string.camera_subtitle)
            overlayView.render(null)
            metrics.setText(R.string.camera_metrics_unavailable)
            cameraStatus.visibility = View.VISIBLE
            replayButton.visibility = View.GONE
            modeButton.setText(R.string.replay_mode)
            refreshCameraState()
        }
    }

    private fun refreshCameraState() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraStatus.setText(R.string.camera_permission)
            cameraButton.visibility = View.VISIBLE
            previewView.visibility = View.INVISIBLE
            return
        }
        cameraButton.visibility = View.GONE
        startCameraPreview()
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
    }

    private fun startCameraPreview() {
        estimator?.close()
        estimator = runCatching {
            val modelFile = File(filesDir, MODEL_FILE_NAME)
            if (!modelFile.exists()) assets.open(MODEL_FILE_NAME).use { input ->
                modelFile.outputStream().use { output -> input.copyTo(output) }
            }
            MoveNetLiteRtEstimator.fromModelFile(modelFile)
        }.getOrNull()
        if (estimator == null) cameraStatus.text = getString(R.string.camera_model_missing, MODEL_FILE_NAME)
        else cameraStatus.setText(R.string.live_loading)
        previewView.visibility = View.VISIBLE
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            runCatching {
                val provider = providerFuture.get()
                cameraProvider = provider
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(cameraExecutor) { image ->
                    try {
                        val model = estimator ?: return@setAnalyzer
                        val planes = image.planes
                        if (planes.size < 3) return@setAnalyzer
                        val rgb = YuvFramePreprocessor.convert(
                            image.width, image.height,
                            planes[0].buffer, planes[1].buffer, planes[2].buffer,
                            planes[0].rowStride, planes[1].rowStride, planes[1].pixelStride,
                            image.imageInfo.rotationDegrees,
                        )
                        val raw = model.estimate(rgb)
                        val frameWidth = image.width
                        val frameHeight = image.height
                        val rotation = image.imageInfo.rotationDegrees
                        val mapped = raw.mapValues { (_, landmark) ->
                            landmark.copy(point = MoveNetCoordinateMapper.map(
                                landmark.point,
                                rotation,
                                frontCamera = true,
                                previewWidth = previewView.width.coerceAtLeast(1),
                                previewHeight = previewView.height.coerceAtLeast(1),
                                sourceWidth = frameWidth,
                                sourceHeight = frameHeight,
                            ))
                        }
                        val timestampNanos = System.nanoTime().coerceAtLeast(0L)
                        val frame = PoseFrame(frameId++, timestampNanos, mapped)
                        val judgmentFrame = PoseFrame(frame.frameId, timestampNanos, raw)
                        val tracking = trackingEvaluator.evaluate(judgmentFrame)
                        // Missing geometry must interrupt transition confirmation, not supply
                        // a synthetic standing angle as a valid observation.
                        val observation = squatObservation(judgmentFrame, tracking)
                            ?: SquatObservation(timestampNanos, 180f, 0.5f, TrackingState.PARTIAL_BODY)
                        val analysis = squatEngine.observe(observation)
                        runOnUiThread {
                            if (mode == DemoMode.CAMERA) {
                                overlayView.render(frame)
                                cameraStatus.setText(R.string.live_running)
                                metrics.text = getString(
                                    R.string.live_pose_metrics,
                                    observation.trackingState.name,
                                    analysis?.phase?.name ?: "UP",
                                    analysis?.repetitionCount ?: 0,
                                    mapped.count { it.value.confidence >= 0.45f },
                                )
                            }
                        }
                    } catch (_: Throwable) {
                        runOnUiThread { if (mode == DemoMode.CAMERA) cameraStatus.setText(R.string.live_error) }
                    } finally {
                        image.close()
                    }
                }
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
            }.onFailure {
                cameraStatus.setText(R.string.camera_unavailable)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun squatObservation(frame: PoseFrame, tracking: TrackingState): SquatObservation? {
        if (tracking != TrackingState.TRACKING) {
            return SquatObservation(frame.timestampNanos, 180f, 0.5f, tracking)
        }
        val points = frame.landmarks
        fun point(type: KeypointType) = points[type]?.takeIf { it.confidence >= 0.45f }?.point
        val sides = listOf(
            Triple(KeypointType.LEFT_HIP, KeypointType.LEFT_KNEE, KeypointType.LEFT_ANKLE),
            Triple(KeypointType.RIGHT_HIP, KeypointType.RIGHT_KNEE, KeypointType.RIGHT_ANKLE),
        ).mapNotNull { (hipType, kneeType, ankleType) ->
            val hip = point(hipType) ?: return@mapNotNull null
            val knee = point(kneeType) ?: return@mapNotNull null
            val ankle = point(ankleType) ?: return@mapNotNull null
            Geometry.angleDegrees(hip, knee, ankle)?.let { angle -> hip.y to angle }
        }
        if (sides.isEmpty()) return null
        val hipY = sides.map { it.first }.average().toFloat()
        val kneeAngle = sides.map { it.second }.average().toFloat()
        return SquatObservation(
            timestampNanos = frame.timestampNanos,
            kneeAngleDegrees = kneeAngle,
            hipY = hipY,
            trackingState = tracking,
        )
    }

    private fun updateReplayMetrics() {
        if (!mode.showReplayMetrics) return
        val state = replayController.state
        if (mode.showSyntheticPose) overlayView.render(replayController.syntheticPoseFrame())
        metrics.text = getString(
            R.string.replay_metrics,
            state.phase,
            state.repetitions,
            state.tracking,
            state.latencyMs,
        )
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
        private const val MODEL_FILE_NAME = "movenet_thunder.tflite"
    }
}
